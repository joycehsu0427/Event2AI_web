package event.to.ai.backend.analysis.application;

import event.to.ai.backend.analysis.domain.Group;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SortGroupByPositionUseCase {
    private List<List<Group>> sortedGroups;

    public SortGroupByPositionUseCase() {}

    public List<List<Group>> sort(List<Group> groups) {
        // First sort groups by Pos.X
        groups.sort(Comparator.comparingDouble(g -> g.getUseCasePos().getX()));

        // compute threshold
        double thresholdX = groups.stream()
                .map(Group::getEventStormingGeo)
                .mapToDouble(Point2D::getX)
                .max()
                .orElse(0);

        double multiple = 0.5;
        double distanceThreshold = multiple * thresholdX;

        sortedGroups = new ArrayList<>();

        for (Group group : groups) {
            Point2D currentPos = group.getUseCasePos();
            boolean addedToExistingGroup = false;

            // Check if you can join an existing group.
            for (List<Group> existingGroup : sortedGroups) {
                // Use the position of the first group in the group as the representative.
                Point2D groupPos = existingGroup.get(0).getUseCasePos();
                double distance = Math.abs(groupPos.getX() - currentPos.getX());

                if (distance < distanceThreshold) {
                    existingGroup.add(group);
                    addedToExistingGroup = true;
                    break;
                }
            }

            // If no suitable group is available, create a new group.
            if (!addedToExistingGroup) {
                List<Group> newGroup = new ArrayList<>();
                newGroup.add(group);
                sortedGroups.add(newGroup);
            }
        }

        // Sort within each List<Group> by Y coordinate (ascending)
        // 在每組內部按照 Y 座標排序（由小到大）
        for (List<Group> groupList : sortedGroups) {
            groupList.sort(Comparator.comparingDouble(g -> g.getUseCasePos().getY()));
        }

        return sortedGroups;
    }

    public List<List<Group>> getSortedGroups() {
        return sortedGroups;
    }
}
