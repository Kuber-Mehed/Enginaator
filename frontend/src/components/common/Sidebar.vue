<template>
  <aside class="col-12 col-lg-auto sidebar-wrapper border-end">
    <div class="sidebar d-flex flex-column h-100 p-3 p-lg-4">
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

      <div class="sidebar-bottom mt-4 mt-lg-auto pt-lg-3">
        <slot name="bottom" />
      </div>
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

.sidebar {
  position: sticky;
  top: 0;
  min-height: 100vh;
}

.sidebar-link {
  color: var(--text-main);
  transition: background-color 0.2s ease, color 0.2s ease;
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

.sidebar-bottom {
  position: sticky;
  bottom: 0;
  background: var(--sidebar-bg);
}

@media (max-width: 991.98px) {
  .sidebar-wrapper {
    width: 100%;
  }

  .sidebar {
    position: static;
    min-height: auto;
  }

  .sidebar-bottom {
    position: static;
  }
}
</style>
