package event.to.ai.backend.analysis.application;

import event.to.ai.backend.analysis.domain.StickyNote;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ClusterByFrameIdUseCase {

    private List<List<StickyNote>> groups = new ArrayList<>();

    public ClusterByFrameIdUseCase() {}

    public void cluster(List<StickyNote> stickyNotes) {
        Map<String, List<StickyNote>> groupMap = new LinkedHashMap<>();

        for (StickyNote note : stickyNotes) {
            if (note.getFrameId() != null) {
                groupMap.computeIfAbsent(note.getFrameId(), k -> new ArrayList<>()).add(note);
            }
        }

        groups = new ArrayList<>(groupMap.values());
    }

    public List<List<StickyNote>> getAllGroups() {
        return groups;
    }

    public int getGroupAmount() {
        return groups.size();
    }
}
