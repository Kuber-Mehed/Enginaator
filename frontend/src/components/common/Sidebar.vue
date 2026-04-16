<template>
  <aside class="col-12 col-lg-auto sidebar-wrapper border-end">
    <div class="sidebar d-flex flex-column justify-content-between h-100 p-3 p-lg-4">
      <div>
        <div class="d-flex align-items-center gap-3 mb-4 mb-lg-5">
          <div class="brand-icon d-flex align-items-center justify-content-center rounded-4 border">
            <i class="bi bi-building"></i>
          </div>
          <span class="fs-4 fw-bold">SVARA</span>
        </div>

        <nav class="nav flex-column gap-2">
          <RouterLink
              v-for="item in items"
              :key="item.to"
              :to="item.to"
              class="nav-link sidebar-link d-flex align-items-center gap-3 rounded-4 px-3 py-3 fw-semibold"
              :class="{ active: currentPath === item.to }"
          >
            <i :class="item.icon"></i>
            <span>{{ item.label }}</span>
          </RouterLink>
        </nav>
      </div>

      <slot name="bottom" />
    </div>
  </aside>
</template>

<script setup lang="ts">
import { RouterLink } from 'vue-router'

interface SidebarItem {
  label: string
  to: string
  icon: string
}

defineProps<{
  items: SidebarItem[]
  currentPath: string
}>()
</script>

<style scoped>
.sidebar-wrapper {
  width: 280px;
  background-color: var(--sidebar-bg);
  border-color: var(--border-color) !important;
}

.sidebar-link {
  color: var(--text-main);
}

.sidebar-link:hover {
  background-color: var(--link-hover-bg);
}

.sidebar-link.active {
  background-color: var(--link-active-bg);
  color: var(--link-active-color);
}

.brand-icon {
  width: 42px;
  height: 42px;
  background-color: var(--brand-bg);
  border-color: var(--brand-border) !important;
  color: var(--brand-color);
}

@media (max-width: 991.98px) {
  .sidebar-wrapper {
    width: 100%;
  }
}
</style>