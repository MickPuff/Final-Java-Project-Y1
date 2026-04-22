import java.time.LocalDate;

class taskFactory {
    public static CheckListItem createTask(String name, String description, LocalDate date, String priority) {
        if (date == null && priority.equalsIgnoreCase("false")) {
            return new BasicTask(name, description);
        } else if (priority.equalsIgnoreCase("false")) {
            return new DueDateTask(new BasicTask(name, description), date);
        } else if (date == null && priority.equalsIgnoreCase("true")) {
            return new PriorityTask(new BasicTask(name, description));
        } else {
            CheckListItem t = new BasicTask(name, description);
            t = new DueDateTask(t, date);
            t = new PriorityTask(t);
            return t;
        }
    }
}