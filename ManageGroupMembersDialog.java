import java.awt.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.stream.Collectors;
import javax.swing.*;

public class ManageGroupMembersDialog extends JDialog {
    private GroupChat groupChat;
    private User currentUser;
    private JList<User> currentMembersList;
    private DefaultListModel<User> currentMembersModel;
    private boolean changesMade = false;

    public ManageGroupMembersDialog(Frame owner, GroupChat groupChat, User currentUser) {
        super(owner, "Manage Members: " + groupChat.getGroupName(), true);
        this.groupChat = groupChat;
        this.currentUser = currentUser;

        setSize(450, 400);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(24, 34, 54));

        // Initialize model and list
        currentMembersModel = new DefaultListModel<>();
        currentMembersList = new JList<>(currentMembersModel);
        populateCurrentMembers();

        // Create the main list panel
        JPanel listPanel = createListPanel("Current Members", currentMembersList);
        
        // --- NEW: Button panel for Add/Remove actions ---
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        actionPanel.setBackground(new Color(24, 34, 54));
        JButton addMembersButton = new JButton("Add Members");
        JButton removeMembersButton = new JButton("Remove Selected");
        actionPanel.add(addMembersButton);
        actionPanel.add(removeMembersButton);

        // --- Bottom panel for Save/Cancel ---
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setBackground(new Color(24, 34, 54));
        JButton saveButton = new JButton("Save Changes");
        JButton cancelButton = new JButton("Cancel");
        bottomPanel.add(saveButton);
        bottomPanel.add(cancelButton);

        add(listPanel, BorderLayout.CENTER);
        add(actionPanel, BorderLayout.NORTH); // Place Add/Remove at the top
        add(bottomPanel, BorderLayout.SOUTH);

        // --- Action Listeners ---
        addMembersButton.addActionListener(e -> showAddMembersDialog());
        removeMembersButton.addActionListener(e -> removeSelectedMembers());
        
        // Disable remove button if creator is selected
        currentMembersList.addListSelectionListener(e -> {
            boolean creatorSelected = false;
            for (User user : currentMembersList.getSelectedValuesList()) {
                if (user.getUsername().equals(DataManager.getGroupChatCreator(groupChat.getGroupId()))) {
                    creatorSelected = true;
                    break;
                }
            }
            removeMembersButton.setEnabled(!creatorSelected);
        });

        saveButton.addActionListener(e -> saveChanges());
        cancelButton.addActionListener(e -> dispose());
    }

    private void populateCurrentMembers() {
        currentMembersModel.clear();
        List<User> currentMembers = DataManager.getGroupChatMembers(groupChat.getGroupId());
        currentMembers.forEach(currentMembersModel::addElement);
    }

    private JPanel createListPanel(String title, JList<User> list) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(title));
        list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        panel.add(new JScrollPane(list), BorderLayout.CENTER);
        return panel;
    }
    
    // --- NEW: Method to handle removing members ---
    private void removeSelectedMembers() {
        List<User> toRemove = currentMembersList.getSelectedValuesList();
        if (toRemove.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select members to remove.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        for (User user : toRemove) {
            currentMembersModel.removeElement(user);
        }
        changesMade = true;
    }

    private void showAddMembersDialog() {
        // Get all possible users that can be in this group chat
        String roleToAdd = "Tutor".equals(currentUser.getRole()) ? "Student" : "Student";
        List<User> allPossibleUsers = DataManager.getAllUsersByRole(roleToAdd);

        // Create a set of current member usernames for quick lookup
        Set<String> currentMemberUsernames = new HashSet<>();
        for (int i = 0; i < currentMembersModel.size(); i++) {
            currentMemberUsernames.add(currentMembersModel.getElementAt(i).getUsername());
        }

        // Filter out users who are already in the group
        List<User> availableUsers = allPossibleUsers.stream()
            .filter(user -> !currentMemberUsernames.contains(user.getUsername()))
            .collect(Collectors.toList());

        if (availableUsers.isEmpty()) {
            JOptionPane.showMessageDialog(this, "There are no more users to add to this group.", "All Users Added", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JList<User> availableList = new JList<>(new Vector<>(availableUsers));
        availableList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        int result = JOptionPane.showConfirmDialog(
            this, new JScrollPane(availableList), "Select Users to Add",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            List<User> usersToAdd = availableList.getSelectedValuesList();
            for (User user : usersToAdd) {
                currentMembersModel.addElement(user); // Add directly to the model
            }
            changesMade = true;
        }
    }

    private void saveChanges() {
        if (!changesMade) {
            dispose(); // No changes to save
            return;
        }

        Set<User> finalMembers = new HashSet<>();
        for (int i = 0; i < currentMembersModel.size(); i++) {
            finalMembers.add(currentMembersModel.getElementAt(i));
        }
        
        // This line is correct, assuming the DataManager method below is also corrected.
        if (DataManager.updateGroupChatMembers(groupChat.getGroupId(), finalMembers)) {
            JOptionPane.showMessageDialog(this, "Group members updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to update group members.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}