export type InventoryCategory =
    | 'housekeeping'
    | 'food'
    | 'beverage'
    | 'amenity'
    | 'maintenance'

export interface InventoryItem {
    id: number
    name: string
    category: InventoryCategory
    unit: string
    quantityInStock: number
    quantityReserved: number
    quantityAvailable: number
    lowStockThreshold: number
}

export interface InventoryFormState {
    id: number | null
    name: string
    category: InventoryCategory
    unit: string
    quantityInStock: number
    quantityReserved: number
    quantityAvailable: number
    lowStockThreshold: number
}