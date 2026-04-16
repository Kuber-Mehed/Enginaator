import type { InventoryCategory, InventoryFormState } from '../types/inventory'

export const INVENTORY_CATEGORIES = [
    'All',
    'Housekeeping',
    'Food',
    'Beverage',
    'Amenity',
    'Maintenance',
] as const

export type InventoryCategoryFilter = typeof INVENTORY_CATEGORIES[number]

export const FORM_CATEGORY_OPTIONS: InventoryCategory[] = [
    'housekeeping',
    'food',
    'beverage',
    'amenity',
    'maintenance',
]

export const EMPTY_INVENTORY_FORM: InventoryFormState = {
    id: null,
    name: '',
    category: 'housekeeping',
    unit: 'piece',
    quantityInStock: 0,
    quantityReserved: 0,
    quantityAvailable: 0,
    lowStockThreshold: 0,
}