public class GroupChat {
    private final String groupId;
    private final String groupName;

    public GroupChat(String groupId, String groupName) {
        this.groupId = groupId;
        this.groupName = groupName;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    // This is important so the group name displays correctly in the UI list.
    @Override
    public String toString() {
        return groupName;
    }
}