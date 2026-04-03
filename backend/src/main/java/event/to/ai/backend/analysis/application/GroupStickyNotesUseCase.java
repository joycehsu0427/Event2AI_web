package event.to.ai.backend.analysis.application;

import event.to.ai.backend.analysis.domain.Group;
import event.to.ai.backend.analysis.domain.StickyNote;

import java.util.List;

public class GroupStickyNotesUseCase {
    private List<Group> groups;
    private List<StickyNote> stickyNotes;
    private List<List<StickyNote>> clusteredStickyNotes;

    public GroupStickyNotesUseCase() {
    }

    public void group(List<StickyNote> stickyNotes) {
        this.stickyNotes = stickyNotes;
        ClusterStickyNotesUseCase clusterStickyNotesUseCase = new ClusterStickyNotesUseCase();
        clusterStickyNotesUseCase.cluster(stickyNotes);
        this.clusteredStickyNotes = clusterStickyNotesUseCase.getAllGroup();
        ClassifyStickNotesUseCase classifyStickNotesUseCase = new ClassifyStickNotesUseCase();
        classifyStickNotesUseCase.classify(this.clusteredStickyNotes);
        this.groups = classifyStickNotesUseCase.getGroups();
    }

    public List<Group> getGroups() {
        return groups;
    }

    public void setGroups(List<Group> groups) {
        this.groups = groups;
    }

    public List<StickyNote> getStickyNotes() {
        return stickyNotes;
    }

    public void setStickyNotes(List<StickyNote> stickyNotes) {
        this.stickyNotes = stickyNotes;
    }

    public List<List<StickyNote>> getClusteredStickyNotes() {
        return clusteredStickyNotes;
    }

    public void setClusteredStickyNotes(List<List<StickyNote>> clusteredStickyNotes) {
        this.clusteredStickyNotes = clusteredStickyNotes;
    }
}
