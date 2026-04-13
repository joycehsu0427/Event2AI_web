<template>
  <v-group
    :config="groupConfig"
    @dblclick="handleDblClick"
  >
    <!-- 1. 外框背景 (使用計算出的高度) -->
    <v-rect :config="rectConfig" />

    <!-- 2. 上方標題區 -->
    <v-text :config="titleConfig" />

    <!-- 3. 中間分割橫線 -->
    <v-line :config="lineConfig" />

    <!-- 4. 下方屬性清單區 -->
    <v-text :config="attributesConfig" />
  </v-group>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { DomainModelItemType, type DomainModelItemElement } from '@/types/elements';
import { useBoardStore } from '@/stores/boardStore';

const props = defineProps<{
  element: DomainModelItemElement;
}>();

const boardStore = useBoardStore();

const TITLE_AREA_HEIGHT = 65;
const ATTR_LINE_HEIGHT = 24;

// 動態計算物件所需的高度
const calculatedHeight = computed(() => {
  const attrCount = props.element.attributes.length;
  const contentHeight = TITLE_AREA_HEIGHT + (attrCount * ATTR_LINE_HEIGHT) + 20; // 20 為底部緩衝
  // 確保高度至少為 100，或與 element.height 取最大值（若使用者手動拉伸過）
  return Math.max(contentHeight, props.element.height, 100);
});

const groupConfig = computed(() => ({
  id: props.element.id,
  x: 0,
  y: 0,
  width: props.element.width,
  height: calculatedHeight.value,
  draggable: false,
}));

const rectConfig = computed(() => ({
  width: props.element.width,
  height: calculatedHeight.value,
  fill: '#ffffff',
  stroke: '#333333',
  strokeWidth: 1.5,
  cornerRadius: 4,
  shadowBlur: 5,
  shadowColor: 'rgba(0,0,0,0.1)',
  shadowOffset: { x: 2, y: 2 },
}));

const titleConfig = computed(() => ({
  text: `<<${props.element.modelType}>>\n${props.element.name}`,
  width: props.element.width,
  height: TITLE_AREA_HEIGHT,
  padding: 8,
  align: 'center',
  verticalAlign: 'middle',
  fontSize: 20,
  fontFamily: 'Arial',
  fontStyle: 'bold',
  fill: '#2c3e50',
  wrap: 'char',
}));

const lineConfig = computed(() => ({
  points: [0, TITLE_AREA_HEIGHT, props.element.width, TITLE_AREA_HEIGHT],
  stroke: '#333333',
  strokeWidth: 1,
}));

const attributesConfig = computed(() => {
  const attrText = props.element.attributes
    .map(attr => {
      if (props.element.modelType === DomainModelItemType.ENUM) {
        return `  ${attr.name}`;
      }
      const typeStr = attr.dataType ? `: ${attr.dataType}` : '';
      const constraintStr = attr.constraint ? ` {${attr.constraint}}` : '';
      return `  ${attr.name}${typeStr}${constraintStr}`;
    })
    .join('\n');

  return {
    text: attrText || '  (no attributes)',
    x: 0,
    y: TITLE_AREA_HEIGHT,
    width: props.element.width,
    height: calculatedHeight.value - TITLE_AREA_HEIGHT,
    padding: 12,
    fontSize: 18,
    fontFamily: 'Courier New',
    fill: '#34495e',
    wrap: 'none',
    lineHeight: 1.3,
  };
});

const handleDblClick = () => {
  boardStore.openDomainModelModal(props.element.id);
};
</script>
