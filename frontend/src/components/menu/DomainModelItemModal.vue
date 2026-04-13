<template>
  <div v-if="boardStore.isDomainModelModalOpen" class="modal-overlay" @click.self="close">
    <div class="modal-content">
      <div class="modal-header">
        <h3>Edit Domain Model</h3>
        <button class="close-btn" @click="close">&times;</button>
      </div>

      <div class="modal-body">
        <!-- Model Type and Name -->
        <div class="form-row">
          <div class="form-group">
            <label>Model Type</label>
            <select v-model="formData.modelType">
              <option v-for="type in modelTypes" :key="type" :value="type">{{ type }}</option>
            </select>
          </div>
          <div class="form-group">
            <label>Model Name</label>
            <input v-model="formData.name" placeholder="Entity Name" />
          </div>
        </div>

        <div class="divider"></div>

        <!-- Attributes List -->
        <div class="attributes-section">
          <div class="section-header">
            <h4>Attributes</h4>
            <button class="add-attr-btn" @click="addAttribute">+ Add</button>
          </div>

          <div class="attributes-list">
            <div v-for="(attr, index) in formData.attributes" :key="index" class="attribute-item">
              <input v-model="attr.name" placeholder="Name" class="attr-name" />
              
              <template v-if="formData.modelType !== DomainModelItemType.ENUM">
                <input v-model="attr.dataType" placeholder="DataType" class="attr-type" />
                <input v-model="attr.constraint" placeholder="Constraint" class="attr-const" />
              </template>

              <button class="remove-attr-btn" @click="removeAttribute(index)">&times;</button>
            </div>
            <div v-if="formData.attributes.length === 0" class="empty-hint">
              No attributes added.
            </div>
          </div>
        </div>
      </div>

      <div class="modal-footer">
        <button class="cancel-btn" @click="close">Cancel</button>
        <button class="save-btn" @click="save">Save Changes</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, reactive } from 'vue';
import { useBoardStore } from '@/stores/boardStore';
import { DomainModelItemType, type DomainModelAttribute } from '@/types/elements';

const boardStore = useBoardStore();

const modelTypes = Object.values(DomainModelItemType);

const formData = reactive({
  name: '',
  modelType: DomainModelItemType.ENTITY,
  attributes: [] as DomainModelAttribute[],
});

// Sync form data with selected element when modal opens
watch(() => boardStore.isDomainModelModalOpen, (isOpen) => {
  if (isOpen && boardStore.getEditingDomainModel) {
    const element = boardStore.getEditingDomainModel;
    formData.name = element.name;
    formData.modelType = element.modelType;
    // Deep copy attributes to avoid direct mutation
    formData.attributes = JSON.parse(JSON.stringify(element.attributes));
  }
});

const addAttribute = () => {
  formData.attributes.push({
    name: '',
    dataType: '',
    constraint: '',
    displayOrder: formData.attributes.length,
  });
};

const removeAttribute = (index: number) => {
  formData.attributes.splice(index, 1);
};

const close = () => {
  boardStore.closeDomainModelModal();
};

const save = () => {
  if (boardStore.editingDomainModelId) {
    // If ENUM, clear dataType and constraint for all attributes
    if (formData.modelType === DomainModelItemType.ENUM) {
      formData.attributes.forEach(attr => {
        attr.dataType = '';
        attr.constraint = '';
      });
    }

    boardStore.updateElement(boardStore.editingDomainModelId, {
      name: formData.name,
      modelType: formData.modelType,
      attributes: [...formData.attributes],
    });
    close();
  }
};
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 2000;
}

.modal-content {
  background: white;
  width: 500px;
  max-width: 90%;
  border-radius: 12px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
  display: flex;
  flex-direction: column;
}

.modal-header {
  padding: 16px 20px;
  border-bottom: 1px solid #eee;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-header h3 {
  margin: 0;
  font-size: 1.2rem;
  color: #333;
}

.close-btn {
  background: none;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
  color: #999;
}

.modal-body {
  padding: 20px;
  max-height: 60vh;
  overflow-y: auto;
}

.form-row {
  display: flex;
  gap: 15px;
  margin-bottom: 20px;
}

.form-group {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.form-group label {
  font-size: 0.85rem;
  font-weight: bold;
  color: #666;
}

.form-group select, .form-group input {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 0.95rem;
}

.divider {
  height: 1px;
  background: #eee;
  margin: 20px 0;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.section-header h4 {
  margin: 0;
  color: #444;
}

.add-attr-btn {
  padding: 4px 12px;
  background: #eef6ff;
  border: 1px solid #007bff;
  color: #007bff;
  border-radius: 4px;
  font-size: 0.85rem;
  cursor: pointer;
}

.attribute-item {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
  align-items: center;
}

.attribute-item input {
  padding: 6px 10px;
  border: 1px solid #eee;
  border-radius: 4px;
  font-size: 0.9rem;
}

.attr-name { flex: 2; }
.attr-type { flex: 1.5; }
.attr-const { flex: 1.5; }

.remove-attr-btn {
  background: none;
  border: none;
  color: #ff4d4f;
  font-size: 1.2rem;
  cursor: pointer;
  padding: 0 5px;
}

.empty-hint {
  text-align: center;
  color: #bbb;
  padding: 20px;
  font-style: italic;
}

.modal-footer {
  padding: 16px 20px;
  border-top: 1px solid #eee;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.save-btn {
  padding: 8px 20px;
  background: #007bff;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
}

.cancel-btn {
  padding: 8px 20px;
  background: #f5f5f5;
  color: #666;
  border: none;
  border-radius: 6px;
  cursor: pointer;
}

.save-btn:hover { background: #0056b3; }
.cancel-btn:hover { background: #e8e8e8; }
</style>
