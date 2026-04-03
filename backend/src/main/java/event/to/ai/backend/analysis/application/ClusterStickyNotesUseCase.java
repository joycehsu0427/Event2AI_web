package event.to.ai.backend.analysis.application;

import event.to.ai.backend.analysis.domain.StickyNote;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.abs;

public class ClusterStickyNotesUseCase {
    private int groupAmount;
    private List<List<StickyNote>> stickyNotesGroup = new ArrayList<>();

    public ClusterStickyNotesUseCase() {}

    public void cluster(List<StickyNote> stickyNotes) {
        this.groupAmount = countGroupAmount(stickyNotes);
    }

    public int getGroupAmount() {
        return this.groupAmount;
    }

    public List<List<StickyNote>> getAllGroup() {
        return stickyNotesGroup;
    }

    public List<StickyNote> getGroupByGroupIdx(int groupIdx) {
        return stickyNotesGroup.get(groupIdx);
    }

    private int countGroupAmount(List<StickyNote> stickyNotes) {
        stickyNotesGroup = groupStickyNotes(stickyNotes);
        groupAmount = stickyNotesGroup.size();
        return groupAmount;
    }

    private static List<List<StickyNote>> groupStickyNotes(List<StickyNote> notes) {
        List<List<StickyNote>> groups = new ArrayList<>();
        boolean[] visited = new boolean[notes.size()];

        for (int i = 0; i < notes.size(); i++) {
            if (!visited[i]) {
                List<StickyNote> group = new ArrayList<>();
                dfs(i, notes, visited, group);
                groups.add(group);
            }
        }

        return groups;
    }

    private static void dfs(int idx, List<StickyNote> notes, boolean[] visited, List<StickyNote> group) {
        visited[idx] = true;
        group.add(notes.get(idx));

        for (int i = 0; i < notes.size(); i++) {
            if (!visited[i] && isAdjacent(notes.get(idx), notes.get(i))) {
                dfs(i, notes, visited, group);
            }
        }
    }

    private static boolean isAdjacent(StickyNote a, StickyNote b) {
        double dx = abs(a.getPos().getX() - b.getPos().getX());
        double dy = abs(a.getPos().getY() - b.getPos().getY());
        double dist = Math.sqrt(dx * dx + dy * dy);

        double sizeA = ( a.getGeo().getX() +  a.getGeo().getY()) / 2;
        double sizeB = ( b.getGeo().getX() +  b.getGeo().getY()) / 2;
        double threshold = 1.5 * Math.max(sizeA, sizeB);

        return dist <= threshold;
    }
}
