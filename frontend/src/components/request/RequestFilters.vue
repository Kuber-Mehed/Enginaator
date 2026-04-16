<template>
  <div
      class="btn-group dashboard-segmented-control p-1 rounded-4"
      role="group"
      aria-label="Request filters"
  >
    <button
        v-for="tab in tabs"
        :key="tab.key"
        type="button"
        class="btn rounded-3 px-3 px-md-4 py-2 fw-semibold"
        :class="modelValue === tab.key ? 'btn-segment-active' : 'btn-segment'"
        @click="$emit('update:modelValue', tab.key)"
    >
      {{ tab.label }} ({{ tab.count }})
    </button>
  </div>
</template>

<script setup lang="ts">
type RequestTab = 'active' | 'all'

interface TabItem {
  key: RequestTab
  label: string
  count: number
}

defineProps<{
  modelValue: RequestTab
  tabs: TabItem[]
}>()

defineEmits<{
  (e: 'update:modelValue', value: RequestTab): void
}>()
</script>

<style scoped>
.dashboard-segmented-control {
  background-color: var(--segment-bg);
  border: 1px solid var(--border-color);
}

.btn-segment {
  color: var(--segment-color);
  background-color: transparent;
  border: none;
}

.btn-segment:hover {
  color: var(--text-main);
}

.btn-segment-active {
  color: var(--segment-active-color);
  background-color: var(--segment-active-bg);
  border: none;
}
</style>