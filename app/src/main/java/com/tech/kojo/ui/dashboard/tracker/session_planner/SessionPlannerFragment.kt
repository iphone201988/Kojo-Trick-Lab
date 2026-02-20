package com.tech.kojo.ui.dashboard.tracker.session_planner

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.tech.kojo.BR
import com.tech.kojo.R
import com.tech.kojo.base.BaseFragment
import com.tech.kojo.base.BaseViewModel
import com.tech.kojo.base.SimpleRecyclerViewAdapter
import com.tech.kojo.data.api.Constants
import com.tech.kojo.data.model.CreateSessionApiResponse
import com.tech.kojo.data.model.GetMonthApiResponse
import com.tech.kojo.data.model.GetNextDateAPiResponse
import com.tech.kojo.data.model.GetPastSessionAPiResponse
import com.tech.kojo.data.model.MonthSessionData
import com.tech.kojo.data.model.NextSessionData
import com.tech.kojo.data.model.PastSessionData
import com.tech.kojo.databinding.AddSessionBottomItemBinding
import com.tech.kojo.databinding.ChooseColorDailogItemBinding
import com.tech.kojo.databinding.FragmentSessionPlannerBinding
import com.tech.kojo.databinding.PastRvItemBinding
import com.tech.kojo.databinding.UpcomingRvItemBinding
import com.tech.kojo.ui.common.CommonActivity
import com.tech.kojo.utils.BaseCustomBottomSheet
import com.tech.kojo.utils.BaseCustomDialog
import com.tech.kojo.utils.BindingUtils
import com.tech.kojo.utils.Status
import com.tech.kojo.utils.showErrorToast
import com.tech.kojo.utils.showInfoToast
import com.tech.kojo.utils.showSuccessToast
import com.google.android.material.bottomsheet.BottomSheetBehavior
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate


@AndroidEntryPoint
@RequiresApi(Build.VERSION_CODES.O)
class SessionPlannerFragment : BaseFragment<FragmentSessionPlannerBinding>() {
    private val viewModel: SessionPlannerFragmentVM by viewModels()

    private lateinit var upcomingAdapter: SimpleRecyclerViewAdapter<NextSessionData, UpcomingRvItemBinding>
    private lateinit var pastAdapter: SimpleRecyclerViewAdapter<PastSessionData, PastRvItemBinding>
    private lateinit var chooseColorDialog: BaseCustomDialog<ChooseColorDailogItemBinding>
    private lateinit var addSessionBottomSheet: BaseCustomBottomSheet<AddSessionBottomItemBinding>
    private lateinit var dayAdapter: CalendarAdapter
    private lateinit var daysList: MutableList<CalendarDay>
    private var currentYear = LocalDate.now().year
    private var isProgress = false
    private var currentMonth = LocalDate.now().monthValue

    private var colorCode = "red"
    private var dateType = ""
    private var currentPage = 1
    private var startX = 0f
    override fun getLayoutResource(): Int {

        return R.layout.fragment_session_planner
    }


    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(view: View) {
        // adapter
        initAdapter()

        // click
        initOnClick()

        // view
        initView()

        // observer
        initObserver()

    }


    /**
     * Method to initialize click
     */
    private var selectedDate: LocalDate = LocalDate.now()
    private fun initOnClick() {
        viewModel.onClick.observe(viewLifecycleOwner) {
            when (it?.id) {
                R.id.ivBack -> {
                    requireActivity().finish()
                }

                R.id.tvViewAll -> {
                    val intent = Intent(requireContext(), CommonActivity::class.java)
                    intent.putExtra("fromWhere", "allSession")
                    startActivity(intent)
                }

                R.id.btnNext -> {
                    currentMonth++
                    if (currentMonth > 12) {
                        currentMonth = 1
                        currentYear++
                    }
                    loadCalendar()
                }

                R.id.btnPrev -> {
                    currentMonth--
                    if (currentMonth < 1) {
                        currentMonth = 12
                        currentYear--
                    }
                    loadCalendar()
                }

                R.id.btnAdd -> {
                    initBottomSheet()
                }

                R.id.ivNext -> {
                    selectedDate = selectedDate.plusDays(1)
                    binding.tvDate.text = BindingUtils.formatDate(selectedDate)

                    val data = HashMap<String, Any>()
                    val date = BindingUtils.formatDateForApi(selectedDate)
                    data["date"] = date

                    viewModel.getSessionDateApi1(Constants.SESSION_PLANNER_DATE, data)
                }

                R.id.ivPrevius -> {
                    selectedDate = selectedDate.minusDays(1)
                    binding.tvDate.text = BindingUtils.formatDate(selectedDate)

                    val data = HashMap<String, Any>()
                    val date = BindingUtils.formatDateForApi(selectedDate)
                    data["date"] = date

                    viewModel.getSessionDateApi1(Constants.SESSION_PLANNER_DATE, data)
                }

            }
        }
    }

    /**
     * Method to initialize view
     */
    private fun initView() {
        val data = HashMap<String, Any>()
        data["month"] = currentMonth
        data["year"] = currentYear
        viewModel.getSessionMonthApi(Constants.SESSION_PLANNER_MONTH, data)
        // adapter
        loadCalendar()
        binding.tvDate.text = BindingUtils.getFormattedToday()


        // refresh
        binding.ssPullRefresh.setColorSchemeResources(
            ContextCompat.getColor(requireContext(), R.color.colorPrimary)
        )
        binding.ssPullRefresh.setOnRefreshListener {
            Handler().postDelayed({
                binding.ssPullRefresh.isRefreshing = false
                isProgress = true
                // api call
                val data = HashMap<String, Any>()
                data["month"] = currentMonth
                data["year"] = currentYear
                viewModel.getSessionMonthApi(Constants.SESSION_PLANNER_MONTH, data)
            }, 2000)
        }
    }


    /**
     * Method to load calendar
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadCalendar() {
        daysList = CalendarUtils.getMonthDays(currentYear, currentMonth)


        daysList.forEach { day ->
            val key = day.date?.toString() ?: ""
            val colors = eventColorMap[key] ?: emptyList()
            day.eventCount = colors.size
            day.eventColors = colors
        }


        dayAdapter = CalendarAdapter(daysList) { date ->
            val formatted = BindingUtils.formatDate(date.toString())
            dateType = formatted
            dayAdapter.updateSelection(date)
            binding.tvDate.text = dateType
            val data = HashMap<String, Any>()
            data["date"] = date
            viewModel.getSessionDateApi(Constants.SESSION_PLANNER_DATE, data)


        }

        binding.rvCalendar.layoutManager = GridLayoutManager(requireContext(), 7)
        binding.rvCalendar.adapter = dayAdapter

        updateMonthTitle()
    }


    /**
     * Method to update month title
     */
    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateMonthTitle() {
        val date = LocalDate.of(currentYear, currentMonth, 1)
        binding.tvCalenderDate.text =
            date.month.name.lowercase().replaceFirstChar { it.uppercase() } + " " + currentYear
    }


    /** api response observer ***/
    private var eventColorMap: MutableMap<String, List<String>> = mutableMapOf()
    private fun initObserver() {
        viewModel.observeCommon.observe(viewLifecycleOwner) {
            when (it?.status) {
                Status.LOADING -> {
                    if (!isProgress) {
                        showLoading()
                    }

                }

                Status.SUCCESS -> {
                    when (it.message) {
                        "createSessionPlannerApi" -> {
                            runCatching {
                                val jsonData = it.data?.toString().orEmpty()
                                val model: CreateSessionApiResponse? =
                                    BindingUtils.parseJson(jsonData)
                                var createSession = model?.data
                                if (createSession != null) {
                                    showSuccessToast("Session created successfully")
                                    addSessionBottomSheet.dismiss()
                                    val data = HashMap<String, Any>()
                                    data["month"] = currentMonth
                                    data["year"] = currentYear
                                    viewModel.getSessionMonthApi(Constants.SESSION_PLANNER_MONTH, data)
                                }
                            }.onFailure { e ->
                                Log.e("apiErrorOccurred", "Error: ${e.message}", e)
                                showErrorToast(e.message.toString())
                            }.also {
                                hideLoading()
                            }
                        }

                        "getSessionMonthApi" -> {
                            runCatching {
                                val jsonData = it.data?.toString().orEmpty()
                                val model: GetMonthApiResponse? = BindingUtils.parseJson(jsonData)
                                var month = model?.data
                                if (month != null) {
                                    val colorMap = BindingUtils.parseMonthColorJson(
                                        jsonData,
                                        currentMonth,
                                        currentYear
                                    )
                                    isProgress = true
                                    eventColorMap =
                                        colorMap.mapValues { listOf(it.value) }.toMutableMap()
                                    // Refresh calendar
                                    loadCalendar()
                                }
                            }.onFailure { e ->
                                Log.e("apiErrorOccurred", "Error: ${e.message}", e)
                                showErrorToast(e.message.toString())
                            }.also {
                                val data = HashMap<String, Any>()
                                val date = BindingUtils.getCurrentDate()
                                data["date"] = date
                                viewModel.getSessionDateApi(Constants.SESSION_PLANNER_DATE, data)
                            }
                        }


                        "getSessionDateApi" -> {
                            runCatching {
                                val jsonData = it.data?.toString().orEmpty()
                                val model: GetNextDateAPiResponse? =
                                    BindingUtils.parseJson(jsonData)
                                var date = model?.data
                                if (date != null) {
                                    upcomingAdapter.list = date
                                    if (upcomingAdapter.list.isNotEmpty()) {
                                        binding.tvNextEmpty.visibility = View.GONE
                                    } else {
                                        binding.tvNextEmpty.visibility = View.VISIBLE
                                    }
                                }
                            }.onFailure { e ->
                                Log.e("apiErrorOccurred", "Error: ${e.message}", e)
                                showErrorToast(e.message.toString())
                            }.also {
                                val data = HashMap<String, Any>()
                                data["page"] = currentPage
                                viewModel.getSessionPastApi(Constants.SESSION_PLANNER, data)
                            }
                        }

                        "getSessionDateApi1" -> {
                            runCatching {
                                val jsonData = it.data?.toString().orEmpty()
                                val model: GetNextDateAPiResponse? =
                                    BindingUtils.parseJson(jsonData)
                                var date = model?.data
                                if (date != null) {
                                    upcomingAdapter.list = date
                                    if (upcomingAdapter.list.isNotEmpty()) {
                                        binding.tvNextEmpty.visibility = View.GONE
                                    } else {
                                        binding.tvNextEmpty.visibility = View.VISIBLE
                                    }
                                }
                            }.onFailure { e ->
                                Log.e("apiErrorOccurred", "Error: ${e.message}", e)
                                showErrorToast(e.message.toString())
                            }.also {
                                hideLoading()
                            }
                        }


                        "getSessionPastApi" -> {
                            runCatching {
                                val jsonData = it.data?.toString().orEmpty()
                                val model: GetPastSessionAPiResponse? =
                                    BindingUtils.parseJson(jsonData)
                                var past = model?.data
                                if (past != null) {
                                    pastAdapter.list = past
                                    if (pastAdapter.list.isNotEmpty()) {
                                        binding.tvPastEmpty.visibility = View.GONE
                                    } else {
                                        binding.tvPastEmpty.visibility = View.VISIBLE

                                    }
                                }
                            }.onFailure { e ->
                                Log.e("apiErrorOccurred", "Error: ${e.message}", e)
                                showErrorToast(e.message.toString())
                            }.also {
                                hideLoading()
                            }
                        }
                    }
                }

                Status.ERROR -> {
                    hideLoading()
                    showErrorToast(it.message.toString())
                }

                else -> {
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun MonthSessionData.toEventColorMap(): Map<LocalDate, List<String>> {

        val map = mutableMapOf<LocalDate, List<String>>()

        this::class.java.declaredFields.forEach { field ->
            field.isAccessible = true
            val key = field.name
            val value = field.get(this) as? List<String>?

            if (!value.isNullOrEmpty()) {
                val date = LocalDate.parse(key)
                map[date] = value
            }
        }

        return map
    }


    /**
     * Initialize adapter
     */
    private fun initAdapter() {
        upcomingAdapter = SimpleRecyclerViewAdapter(R.layout.upcoming_rv_item, BR.bean) { v, m, _ ->
            when (v?.id) {
                R.id.clSession -> {
                    val intent = Intent(requireContext(), CommonActivity::class.java)
                    intent.putExtra("fromWhere", "sessionDetails")
                    intent.putExtra("sessionData", m)
                    startActivity(intent)
                }
            }

        }
        binding.rvUpcoming.adapter = upcomingAdapter


        pastAdapter = SimpleRecyclerViewAdapter(R.layout.past_rv_item, BR.bean) { v, m, _ ->
            when (v?.id) {
                R.id.clSession -> {
                    val intent = Intent(requireContext(), CommonActivity::class.java)
                    intent.putExtra("fromWhere", "pastSession")
                    intent.putExtra("pastSessionData", m)
                    startActivity(intent)
                }
            }

        }
        binding.rvPast.adapter = pastAdapter

    }

    /**
     * Initialize bottom sheet
     */
    private fun initBottomSheet() {
        addSessionBottomSheet = BaseCustomBottomSheet(
            requireContext(), R.layout.add_session_bottom_item
        ) { view ->
            when (view?.id) {
                R.id.ivSelectColor -> {
                    initDialog()
                }

                R.id.btnSave -> {
                    val sessionTitle =
                        addSessionBottomSheet.binding.etSessionTitle.text.toString().trim()
                    val enterNote = addSessionBottomSheet.binding.etEnterNote.text.toString().trim()
                    if (sessionTitle.isEmpty()) {
                        showInfoToast("Please add session title")
                    } else if (enterNote.isEmpty()) {
                        showInfoToast("Please add note")
                    } else {
                        val data = HashMap<String, Any>()
                        data["title"] = sessionTitle
                        data["note"] = enterNote
                        data["color"] = colorCode
                        if (dateType.isEmpty()) {
                            val dateType = BindingUtils.getCurrentDate()
                            data["date"] = dateType
                        } else {
                            val result = BindingUtils.convertToApiFormat(dateType)
                            data["date"] = result
                        }

                        viewModel.createSessionPlannerApi(Constants.SESSION_CREATE, data)

                    }

                }
            }
        }
        addSessionBottomSheet.behavior.isDraggable = true
        addSessionBottomSheet.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        addSessionBottomSheet.show()

        if (dateType.isEmpty()) {
            addSessionBottomSheet.binding.tvTitle2.text = BindingUtils.getFormattedToday()
        } else {
            addSessionBottomSheet.binding.tvTitle2.text = dateType
        }

    }


    /**
     * choose color dialog initialize
     */
    private fun initDialog() {
        chooseColorDialog = BaseCustomDialog(requireContext(), R.layout.choose_color_dailog_item) {
            when (it?.id) {
                R.id.tvRed -> {
                    colorCode = "red"
                    addSessionBottomSheet.binding.ivSelectColor.setBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(), R.color.red_color
                        )
                    )
                    chooseColorDialog.dismiss()
                }

                R.id.tvGreen -> {
                    colorCode = "green"
                    addSessionBottomSheet.binding.ivSelectColor.setBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(), R.color.green_color
                        )
                    )
                    chooseColorDialog.dismiss()
                }

                R.id.tvOrange -> {
                    colorCode = "orange"
                    addSessionBottomSheet.binding.ivSelectColor.setBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(), R.color.orange
                        )
                    )
                    chooseColorDialog.dismiss()
                }

                R.id.tvBlue -> {
                    colorCode = "blue"
                    addSessionBottomSheet.binding.ivSelectColor.setBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(), R.color.purple
                        )
                    )
                    chooseColorDialog.dismiss()
                }

                R.id.tvCancel -> {
                    colorCode = "red"
                    chooseColorDialog.dismiss()
                }


            }
        }
        chooseColorDialog.setCancelable(true)
        chooseColorDialog.create()
        chooseColorDialog.show()

    }
}