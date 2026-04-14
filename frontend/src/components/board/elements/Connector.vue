<template>
  <v-group>
    <!-- The main connector arrow -->
    <v-arrow
      v-if="points.length >= 4"
      :config="connectorConfig"
      @click="handleClick"
      @tap="handleClick"
    />

    <!-- Start & End Anchors (Visible only when selected) -->
    <template v-if="isSelected">
      <!-- Start Anchor -->
      <v-circle
        :config="getAnchorConfig(points[0], points[1], 'from')"
        @dragmove="handleAnchorDragMove($event, 'from')"
        @dragend="handleAnchorDragEnd($event, 'from')"
      />
      <!-- End Anchor -->
      <v-circle
        :config="getAnchorConfig(points[points.length-2], points[points.length-1], 'to')"
        @dragmove="handleAnchorDragMove($event, 'to')"
        @dragend="handleAnchorDragEnd($event, 'to')"
      />
    </template>
  </v-group>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue';
import { useBoardStore } from '@/stores/boardStore';
import { ElementType, type ConnectorElement, ConnectorTargetType, ConnectorAnchorSide, ConnectorArrowType } from '@/types/elements';

const props = defineProps<{
  element: ConnectorElement;
}>();

const boardStore = useBoardStore();

// Local state for immediate feedback while dragging anchors
const dragStatus = reactive({
  from: null as { x: number, y: number } | null,
  to: null as { x: number, y: number } | null,
});

const isSelected = computed(() => boardStore.selectedElementIds.includes(props.element.id));

const getElementAnchor = (targetId: string, side: ConnectorAnchorSide, offset: number = 0.5) => {
  const el = boardStore.getElementById(targetId);
  if (!el) return { x: 0, y: 0 };

  let x = el.x;
  let y = el.y;

  switch (side) {
    case ConnectorAnchorSide.TOP:
      x += el.width * offset;
      break;
    case ConnectorAnchorSide.RIGHT:
      x += el.width;
      y += el.height * offset;
      break;
    case ConnectorAnchorSide.BOTTOM:
      x += el.width * offset;
      y += el.height;
      break;
    case ConnectorAnchorSide.LEFT:
      y += el.height * offset;
      break;
  }

  return { x, y };
};

const points = computed(() => {
  let fromX = 0, fromY = 0, toX = 0, toY = 0;

  // From Point calculation
  if (dragStatus.from) {
    fromX = dragStatus.from.x;
    fromY = dragStatus.from.y;
  } else if (props.element.fromTargetType === ConnectorTargetType.FREE_POINT && props.element.fromPoint) {
    fromX = props.element.fromPoint.x;
    fromY = props.element.fromPoint.y;
  } else if (props.element.fromTargetId && props.element.fromSide) {
    const anchor = getElementAnchor(props.element.fromTargetId, props.element.fromSide, props.element.fromOffset);
    fromX = anchor.x;
    fromY = anchor.y;
  }

  // To Point calculation
  if (dragStatus.to) {
    toX = dragStatus.to.x;
    toY = dragStatus.to.y;
  } else if (props.element.toTargetType === ConnectorTargetType.FREE_POINT && props.element.toPoint) {
    toX = props.element.toPoint.x;
    toY = props.element.toPoint.y;
  } else if (props.element.toTargetId && props.element.toSide) {
    const anchor = getElementAnchor(props.element.toTargetId, props.element.toSide, props.element.toOffset);
    toX = anchor.x;
    toY = anchor.y;
  }

  return [fromX, fromY, toX, toY];
});

const connectorConfig = computed(() => ({
  id: props.element.id,
  type: ElementType.Connector,
  points: points.value,
  stroke: props.element.strokeColor,
  strokeWidth: props.element.strokeWidth,
  hitStrokeWidth: 20,
  dash: props.element.dashed ? [5, 5] : undefined,
  pointerLength: props.element.endArrow === ConnectorArrowType.TRIANGLE ? 10 : 0,
  pointerWidth: props.element.endArrow === ConnectorArrowType.TRIANGLE ? 10 : 0,
  fill: props.element.strokeColor,
  opacity: isSelected.value ? 1 : 0.8,
  lineJoin: 'round',
  lineCap: 'round',
  draggable: false,
}));

// --- Anchor Logic ---

const getAnchorConfig = (x: number, y: number, type: 'from' | 'to') => ({
  x,
  y,
  radius: 6,
  fill: '#007bff',
  stroke: 'white',
  strokeWidth: 2,
  draggable: true,
  name: `anchor-${type}`,
  shadowBlur: 4,
});

const handleAnchorDragMove = (e: any, type: 'from' | 'to') => {
  const node = e.target;
  // Update local drag status for real-time line update
  dragStatus[type] = {
    x: node.x(),
    y: node.y()
  };
};

const handleAnchorDragEnd = (e: any, type: 'from' | 'to') => {
  const stage = e.target.getStage();
  const pointer = stage.getPointerPosition();
  
  // Clear local drag status first
  dragStatus[type] = null;

  if (!pointer) return;

  // Transform screen coordinates to stage coordinates
  const stageX = (pointer.x - stage.x()) / stage.scaleX();
  const stageY = (pointer.y - stage.y()) / stage.scaleY();

  // Find if mouse is over another element
  const target = stage.getIntersection({ x: pointer.x, y: pointer.y });
  const isElement = target && target !== stage && !target.name().startsWith('anchor-');
  
  let targetId: string | undefined = undefined;
  let targetType = ConnectorTargetType.FREE_POINT;
  let side: ConnectorAnchorSide | undefined = undefined;

  if (isElement) {
    const node = target;
    // Find parent board element group
    const groupNode = node.findAncestor('.board-element-group') || node.parent;
    targetId = groupNode?.id();
    const elementType = (groupNode?.attrs as any)?.type;
    targetType = mapElementTypeToConnectorTargetType(elementType);
    side = ConnectorAnchorSide.LEFT; // Default snap side, can be optimized later
  }

  const updates: any = {};
  if (type === 'from') {
    updates.fromTargetType = targetType;
    updates.fromTargetId = targetId;
    updates.fromSide = side;
    updates.fromPoint = !targetId ? { x: stageX, y: stageY } : undefined;
  } else {
    updates.toTargetType = targetType;
    updates.toTargetId = targetId;
    updates.toSide = side;
    updates.toPoint = !targetId ? { x: stageX, y: stageY } : undefined;
  }

  boardStore.updateElement(props.element.id, updates);
};

const mapElementTypeToConnectorTargetType = (type: string): ConnectorTargetType => {
  switch(type) {
    case ElementType.StickyNote: return ConnectorTargetType.STICKY_NOTE;
    case ElementType.Text: return ConnectorTargetType.TEXT_BOX;
    case ElementType.Frame: return ConnectorTargetType.FRAME;
    case ElementType.DomainModelItem: return ConnectorTargetType.DOMAIN_MODEL_ITEM;
    default: return ConnectorTargetType.FREE_POINT;
  }
};

const handleClick = (e: any) => {
  e.cancelBubble = true;
  boardStore.selectElement(props.element.id, e.evt.ctrlKey || e.evt.metaKey);
};
</script>
