public class ChatListItem {
    private final Object item;
    private final boolean isGroup;

    public ChatListItem(Object item) {
        if (!(item instanceof User) && !(item instanceof GroupChat)) {
            throw new IllegalArgumentException("Item must be a User or GroupChat");
        }
        this.item = item;
        this.isGroup = item instanceof GroupChat;
    }

    public Object getItem() {
        return item;
    }

    public boolean isGroup() {
        return isGroup;
    }

    public String getDisplayName() {
        if (isGroup) {
            return ((GroupChat) item).getGroupName();
        } else {
            return ((User) item).getFullName();
        }
    }

    public String getDisplaySubtext() {
        if (isGroup) {
            return "Group Chat";
        } else {
            return ((User) item).getRole();
        }
    }
    
    @Override
    public String toString() {
        return getDisplayName();
    }
}