import { createRouter, createWebHistory } from 'vue-router'
import GuestView from '../views/GuestView.vue'
import StaffDashboard from '../views/StaffDashboard.vue'
import InventoryView from '../views/InventoryView.vue'
import ReportsView from '../views/ReportsView.vue'

const router = createRouter({
    history: createWebHistory(),
    routes: [
        {
            path: '/',
            redirect: '/guest/101',
        },
        {
            path: '/guest/:room',
            name: 'guest',
            component: GuestView,
            props: true,
        },
        {
            path: '/staff',
            name: 'staff',
            component: StaffDashboard,
        },
        {
            path: '/staff/inventory',
            name: 'inventory',
            component: InventoryView,
        },
        {
            path: '/staff/reports',
            name: 'reports',
            component: ReportsView,
        },
    ],
})

export default router