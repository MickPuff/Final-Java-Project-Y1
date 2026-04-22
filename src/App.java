import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class App {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new TaskManagerGUI();
        });
    }
}

class TaskManagerGUI extends JFrame implements TaskManagerListener {
    private taskManager manager;
    private JList<String> taskList;
    private DefaultListModel<String> listModel;
    private JButton createBtn, priorityBtn, dueDateBtn, removeBtn, newWindowBtn;
    private JLabel taskCountLabel, dateLabel;
    private static final DateTimeFormatter FORMATTER = 
        DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public TaskManagerGUI() {
        manager = taskManager.getInstance();
        
        setTitle("Task Manager");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(600, 500);
        setLocationRelativeTo(null);
        setResizable(true);
        
        // Register as listener for real-time updates
        manager.addListener(this);
        
        // Remove listener when window closes
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                manager.removeListener(TaskManagerGUI.this);
            }
        });

        // Main panel with padding
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        // Title panel with date
        JPanel titlePanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("Task Manager");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        dateLabel = new JLabel("Today: " + LocalDate.now().format(FORMATTER));
        dateLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        titlePanel.add(titleLabel, BorderLayout.WEST);
        titlePanel.add(dateLabel, BorderLayout.EAST);
        mainPanel.add(titlePanel, BorderLayout.NORTH);

        // Center: Task List
        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
        
        listModel = new DefaultListModel<>();
        taskList = new JList<>(listModel);
        taskList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        taskList.setFont(new Font("Arial", Font.PLAIN, 12));
        taskList.setBackground(new Color(240, 240, 240));
        
        // Add custom cell renderer for color highlighting
        taskList.setCellRenderer(new TaskListCellRenderer(manager));
        
        JScrollPane scrollPane = new JScrollPane(taskList);
        scrollPane.setBorder(new TitledBorder("Tasks"));
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Task count
        taskCountLabel = new JLabel("Total Tasks: 0");
        taskCountLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        centerPanel.add(taskCountLabel, BorderLayout.SOUTH);
        
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // Button Panel
        JPanel buttonPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        buttonPanel.setBorder(new TitledBorder("Actions"));
        
        createBtn = createButton("Create Task", e -> showCreateTaskDialog());
        priorityBtn = createButton("Add Priority", e -> addPriority());
        dueDateBtn = createButton("Add Due Date", e -> addDueDate());
        removeBtn = createButton("Remove Task", e -> removeTask());
        newWindowBtn = createButton("Open New Window", e -> openNewWindow());
        JButton exitBtn = createButton("Exit", e -> System.exit(0));
        
        buttonPanel.add(createBtn);
        buttonPanel.add(priorityBtn);
        buttonPanel.add(dueDateBtn);
        buttonPanel.add(removeBtn);
        buttonPanel.add(newWindowBtn);
        buttonPanel.add(exitBtn);
        
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
        setVisible(true);
        
        // Refresh list initially
        refreshTaskList();
    }

    private JButton createButton(String text, ActionListener listener) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.PLAIN, 11));
        btn.addActionListener(listener);
        return btn;
    }

    private void showCreateTaskDialog() {
        JDialog dialog = new JDialog(this, "Create New Task", true);
        dialog.setSize(400, 280);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        JLabel nameLabel = new JLabel("Task Name:");
        JTextField nameField = new JTextField();
        
        JLabel descLabel = new JLabel("Description:");
        JTextField descField = new JTextField();
        
        JLabel dateLabel = new JLabel("Due Date (dd/MM/yyyy):");
        JTextField dateField = new JTextField();
        
        JLabel priorityLabel = new JLabel("Is Priority?");
        JComboBox<String> priorityCombo = new JComboBox<>(new String[]{"false", "true"});
        
        panel.add(nameLabel);
        panel.add(nameField);
        panel.add(descLabel);
        panel.add(descField);
        panel.add(dateLabel);
        panel.add(dateField);
        panel.add(priorityLabel);
        panel.add(priorityCombo);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        JButton createButton = new JButton("Create");
        JButton cancelButton = new JButton("Cancel");
        
        createButton.addActionListener(e -> {
            String name = nameField.getText().trim();
            String desc = descField.getText().trim();
            String dateInput = dateField.getText().trim();
            String priority = (String) priorityCombo.getSelectedItem();
            
            if (name.isEmpty() || desc.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Name and Description are required!", 
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            LocalDate parsedDate = null;
            if (!dateInput.isEmpty()) {
                try {
                    parsedDate = LocalDate.parse(dateInput, FORMATTER);
                } catch (DateTimeParseException ex) {
                    JOptionPane.showMessageDialog(dialog, "Invalid date format! Use dd/MM/yyyy", 
                        "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            
            CheckListItem task = taskFactory.createTask(name, desc, parsedDate, priority);
            if (task != null) {
                manager.addTasks(task);
                refreshTaskList();
                JOptionPane.showMessageDialog(dialog, "Task created successfully!");
                dialog.dispose();
            }
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(createButton);
        buttonPanel.add(cancelButton);
        
        panel.add(new JLabel());
        panel.add(buttonPanel);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void addPriority() {
        int selectedIndex = taskList.getSelectedIndex();
        if (selectedIndex == -1) {
            JOptionPane.showMessageDialog(this, "Please select a task!", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        CheckListItem selectedTask = manager.showList().get(selectedIndex);
        
        // Check if task already has priority
        if (TaskDecorator.hasDecorator(selectedTask, PriorityTask.class)) {
            JOptionPane.showMessageDialog(this, selectedTask.getName() + " already has priority!", 
                "Duplicate Decorator", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        manager.removeTask(selectedTask);
        CheckListItem priorityTask = new PriorityTask(selectedTask);
        manager.addTasks(priorityTask);
        refreshTaskList();
        JOptionPane.showMessageDialog(this, selectedTask.getName() + " is now a priority!");
    }

    private void addDueDate() {
        int selectedIndex = taskList.getSelectedIndex();
        if (selectedIndex == -1) {
            JOptionPane.showMessageDialog(this, "Please select a task!", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        CheckListItem selectedTask = manager.showList().get(selectedIndex);
        
        // Check if task already has due date
        if (TaskDecorator.hasDecorator(selectedTask, DueDateTask.class)) {
            JOptionPane.showMessageDialog(this, selectedTask.getName() + " already has a due date!", 
                "Duplicate Decorator", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String dateInput = JOptionPane.showInputDialog(this, "Enter due date (dd/MM/yyyy):", "");
        if (dateInput != null && !dateInput.trim().isEmpty()) {
            try {
                LocalDate date = LocalDate.parse(dateInput, FORMATTER);
                manager.removeTask(selectedTask);
                CheckListItem dueDateTask = new DueDateTask(selectedTask, date);
                manager.addTasks(dueDateTask);
                refreshTaskList();
                JOptionPane.showMessageDialog(this, selectedTask.getName() + " due date set to: " + dateInput);
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(this, "Invalid date format! Use dd/MM/yyyy", 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void removeTask() {
        int selectedIndex = taskList.getSelectedIndex();
        if (selectedIndex == -1) {
            JOptionPane.showMessageDialog(this, "Please select a task!", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        CheckListItem selectedTask = manager.showList().get(selectedIndex);
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Remove task: " + selectedTask.getName() + "?", 
            "Confirm Removal", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            manager.removeTask(selectedTask);
            refreshTaskList();
        }
    }

    private void openNewWindow() {
        SwingUtilities.invokeLater(() -> {
            new TaskManagerGUI();
        });
    }

    private void refreshTaskList() {
        listModel.clear();
        for (CheckListItem task : manager.showList()) {
            listModel.addElement(task.getDescription());
        }
        taskCountLabel.setText("Total Tasks: " + manager.showList().size());
    }

    @Override
    public void onTasksChanged() {
        SwingUtilities.invokeLater(() -> {
            refreshTaskList();
        });
    }
}

// Custom cell renderer for task list with color highlighting
class TaskListCellRenderer extends DefaultListCellRenderer {
    private taskManager manager;

    public TaskListCellRenderer(taskManager manager) {
        this.manager = manager;
    }

    @Override
    public java.awt.Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                           boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

        if (!isSelected) {
            // Check if task is due within 7 days
            if (index >= 0 && index < manager.showList().size()) {
                CheckListItem task = manager.showList().get(index);
                if (isDueSoon(task)) {
                    setBackground(new Color(255, 200, 200)); // Light red
                    setOpaque(true);
                } else {
                    setBackground(list.getBackground());
                }
            }
        }

        return this;
    }

    private boolean isDueSoon(CheckListItem task) {
        if (task instanceof DueDateTask) {
            long daysUntil = ((DueDateTask) task).getDaysUntilDue();
            return daysUntil >= 0 && daysUntil <= 7;
        }
        if (task instanceof TaskDecorator) {
            return isDueSoon(((TaskDecorator) task).getWrappedTask());
        }
        return false;
    }
}
