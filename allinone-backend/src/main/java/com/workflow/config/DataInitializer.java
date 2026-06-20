package com.workflow.config;

import com.workflow.entity.*;
import com.workflow.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.time.*;

@Component
@Profile("dev")
public class DataInitializer implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    private final EmployeeRepository employeeRepository;
    private final BranchRepository branchRepository;
    private final EmailAllowListRepository emailAllowListRepository;
    private final PasswordEncoder passwordEncoder;
    private final DailyLogRepository dailyLogRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final TaskRepository taskRepository;
    private final HolidayRepository holidayRepository;
    private final NotificationRepository notificationRepository;

    public DataInitializer(EmployeeRepository er, BranchRepository br,
                           EmailAllowListRepository eal, PasswordEncoder pe,
                           DailyLogRepository dlr, LeaveRequestRepository lrr,
                           TaskRepository tr, HolidayRepository hr,
                           NotificationRepository nr) {
        this.employeeRepository = er; this.branchRepository = br;
        this.emailAllowListRepository = eal; this.passwordEncoder = pe;
        this.dailyLogRepository = dlr; this.leaveRequestRepository = lrr;
        this.taskRepository = tr; this.holidayRepository = hr;
        this.notificationRepository = nr;
    }

    @Override
    public void run(String... args) {
        if (employeeRepository.count() > 0) return;

        emailAllowListRepository.save(new EmailAllowList("admin@workflow.com"));
        emailAllowListRepository.save(new EmailAllowList("employee@workflow.com"));
        emailAllowListRepository.save(new EmailAllowList("manager@workflow.com"));

        Branch hq = branchRepository.save(new Branch("Head Office", "HQ", "Kathmandu, Nepal"));
        Branch pkh = branchRepository.save(new Branch("Pokhara Branch", "PKH", "Pokhara, Nepal"));

        Employee admin = new Employee("admin@workflow.com", "Super Admin", Role.SUPER_ADMIN, hq,
                null, AuthType.LOCAL_ONLY, passwordEncoder.encode("admin123"),
                true, true, null, null, null, BigDecimal.valueOf(20), BigDecimal.ZERO, BigDecimal.valueOf(5000));
        admin.setIsOwner(true);
        employeeRepository.save(admin);

        Employee manager = employeeRepository.save(new Employee("manager@workflow.com", "Branch Manager", Role.BRANCH_MANAGER, hq,
                null, AuthType.LOCAL_ONLY, passwordEncoder.encode("manager123"),
                true, true, null, null, null, BigDecimal.valueOf(18), BigDecimal.ZERO, BigDecimal.valueOf(5000)));

        Employee employee = employeeRepository.save(new Employee("employee@workflow.com", "Field Employee", Role.FIELD_EMPLOYEE, hq,
                null, AuthType.LOCAL_ONLY, passwordEncoder.encode("emp123"),
                true, true, WageType.DAILY, BigDecimal.valueOf(1500), null, BigDecimal.valueOf(15), BigDecimal.ZERO, BigDecimal.valueOf(5000)));

        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.withDayOfMonth(1);

        DailyLog log1 = new DailyLog();
        log1.setEmployee(employee);
        log1.setBranch(hq);
        log1.setLogDate(monthStart.plusDays(2));
        log1.setStartTime(LocalTime.of(9, 0));
        log1.setEndTime(LocalTime.of(17, 0));
        log1.setHoursWorked(BigDecimal.valueOf(8));
        log1.setCategory(LogCategory.NEW_FIBER_CONNECTION);
        log1.setWorkDescription("Installed fiber connection at customer site");
        log1.setStatus(LogStatus.APPROVED);
        log1.setReviewedBy(manager);
        log1.setReviewComment("Good work");
        log1.setReviewedAt(LocalDateTime.now());
        log1.setMonthLocked(false);
        log1.setIsHolidayOvertime(false);
        dailyLogRepository.save(log1);

        DailyLog log2 = new DailyLog();
        log2.setEmployee(employee);
        log2.setBranch(hq);
        log2.setLogDate(monthStart.plusDays(5));
        log2.setStartTime(LocalTime.of(8, 30));
        log2.setEndTime(LocalTime.of(16, 30));
        log2.setHoursWorked(BigDecimal.valueOf(8));
        log2.setCategory(LogCategory.SERVICE_MAINTENANCE);
        log2.setWorkDescription("Routine maintenance at office");
        log2.setStatus(LogStatus.PENDING);
        log2.setMonthLocked(false);
        log2.setIsHolidayOvertime(false);
        dailyLogRepository.save(log2);

        LeaveRequest leave = new LeaveRequest();
        leave.setEmployee(employee);
        leave.setLeaveType(LeaveType.ANNUAL);
        leave.setStartDate(today.plusDays(10));
        leave.setEndDate(today.plusDays(12));
        leave.setDurationDays(3);
        leave.setReason("Family vacation");
        leave.setStatus(LeaveStatus.APPROVED);
        leave.setReviewedBy(manager);
        leave.setReviewComment("Approved");
        leave.setDeductedFromBalance(true);
        leave.setReviewedAt(LocalDateTime.now());
        leaveRequestRepository.save(leave);

        Task task = new Task();
        task.setAssignedBy(manager);
        task.setAssignedTo(employee);
        task.setTitle("Install new router at client site");
        task.setDescription("Customer requested router configuration for new fiber connection");
        task.setPriority(TaskPriority.HIGH);
        task.setStatus(TaskStatus.OPEN);
        task.setScheduledDate(today.plusDays(1));
        task.setCustomerName("Ram Sharma");
        task.setCustomerPhone("9841234567");
        task.setCustomerAddress("Kathmandu");
        taskRepository.save(task);

        Holiday holiday = new Holiday();
        holiday.setDate(today.plusDays(30));
        holiday.setName("Dashain");
        holiday.setIsRecurringYearly(true);
        holiday.setOvertimeApplies(false);
        holiday.setCreatedBy(admin);
        holidayRepository.save(holiday);

        Notification notification = new Notification();
        notification.setRecipient(employee);
        notification.setType("TASK_ASSIGNED");
        notification.setTitle("New Task Assigned");
        notification.setBody("You have been assigned a new task: Install new router at client site");
        notification.setRelatedEntityType("Task");
        notification.setRelatedEntityId(task.getId());
        notification.setIsRead(false);
        notificationRepository.save(notification);

        log.info("Seed data created");
    }
}
