package event.to.ai.backend.analysis.application;

import event.to.ai.backend.analysis.domain.Group;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SortGroupByAggregateUseCase {
    private Map<String, List<Group>>  sortedGroups;

    public SortGroupByAggregateUseCase() {}

    public void sort(List<Group> groups) {
        sortedGroups = new HashMap<>();
        for (Group group : groups) {
            if (sortedGroups.containsKey(group.getAggregateName())) {
                sortedGroups.get(group.getAggregateName()).add(group);
            }
            else  {
                List<Group> groupList = new ArrayList<>();
                groupList.add(group);
                sortedGroups.put(group.getAggregateName(), groupList);
            }
        }
    }

    public Map<String, List<Group>> getSortedGroups() {
        return sortedGroups;
    }
}
