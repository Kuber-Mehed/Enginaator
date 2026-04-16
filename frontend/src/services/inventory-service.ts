import api from './api'
import type {InventoryItem} from "@/utils/types/inventory.ts";

// GET /api/staff/inventory
export const getInventory = async (): Promise<InventoryItem[]> => {
    const response = await api.get('/staff/inventory')
    return response.data
}

// POST /api/staff/inventory/item
export const addItem = async (item: InventoryItem) => {
    await api.post('/staff/inventory/item', item)
}

// PUT /api/staff/inventory/item/{itemId}
export const updateItem = async (itemId: string, item: InventoryItem) => {
    await api.put(`/staff/inventory/item/${itemId}`, item)
}

// DELETE /api/staff/inventory/item/{itemId}
export const deleteItem = async (itemId: string) => {
    await api.delete(`/staff/inventory/item/${itemId}`)
}