<template>
  <v-group
    :config="groupConfig"
    @dragmove="handleDragMove"
    @dragend="handleDragEnd"
    @transformend="handleTransformEnd"
    @click="handleClick"
    @dblclick="handleDblClick"
  >
    <!-- Background Rectangle -->
    <v-rect :config="rectConfig" />

    <!-- Title Area: <<type>> and Name -->
    <v-text :config="titleConfig" />

    <!-- Divider Line -->
    <v-line :config="lineConfig" />

    <!-- Attributes Area -->
    <v-text :config="attributesConfig" />
  </v-group>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import type { DomainEntityElement } from '@/types/elements';
import { useBoardStore } from '@/stores/boardStore';

const props = defineProps<{
  element: DomainEntityElement;
}>();

const boardStore = useBoardStore();

const TITLE_HEIGHT = 40; // Fixed height for title section

const groupConfig = computed(() => ({
  id: props.element.id,
  x: props.element.x,
  y: props.element.y,
  width: props.element.width,
  height: props.element.height,
  draggable: props.element.draggable,
}));

const rectConfig = computed(() => ({
  width: props.element.width,
  height: props.element.height,
  fill: '#ffffff',
  stroke: '#333333',
  strokeWidth: 2,
  cornerRadius: 2,
}));

const titleConfig = computed(() => ({
  text: `<<${props.element.modelType}>>\n${props.element.name}`,
  width: props.element.width,
  height: TITLE_HEIGHT,
  padding: 10,
  align: 'center',
  verticalAlign: 'middle',
  fontSize: 14,
  fontStyle: 'bold',
  fill: '#333333',
}));

const lineConfig = computed(() => ({
  points: [0, TITLE_HEIGHT, props.element.width, TITLE_HEIGHT],
  stroke: '#333333',
  strokeWidth: 1,
}));

const attributesConfig = computed(() => {
  const attrText = props.element.attributes
    .map(attr => {
      if (props.element.modelType === 'ENUM') return attr.name;
      return `${attr.name}: ${attr.dataType || 'any'} ${attr.constraint || ''}`;
    })
    .join('\n');

  return {
    text: attrText,
    x: 0,
    y: TITLE_HEIGHT,
    width: props.element.width,
    padding: 10,
    fontSize: 12,
    fill: '#333333',
  };
});

// --- Event Handlers ---

const handleDragMove = (e: any) => {
};

const handleDragEnd = (e: any) => {
  boardStore.updateElement(props.element.id, {
    x: e.target.x(),
    y: e.target.y(),
  });
};

const handleTransformEnd = (e: any) => {
  const node = e.target;
  boardStore.updateElement(props.element.id, {
    x: node.x(),
    y: node.y(),
    width: node.width() * node.scaleX(),
    height: node.height() * node.scaleY(),
  });
  node.scaleX(1);
  node.scaleY(1);
};

const handleClick = (e: any) => {
  e.cancelBubble = true;
  boardStore.selectElement(props.element.id, e.evt.ctrlKey || e.evt.metaKey);
};

const handleDblClick = () => {
  console.log('Double clicked Domain Entity:', props.element.id);
  // This will be used to open the modal
};
</script>
