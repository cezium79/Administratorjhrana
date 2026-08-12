<template>
  <div class="modal-overlay" @click.self="$emit('close')">
    <div class="modal">
      <div class="modal-header">
        <h2>{{ report.title || 'Отчёт #' + report.id }}</h2>
        <button class="modal-close" @click="$emit('close')">×</button>
      </div>

      <div class="modal-body">
        <!-- Tabs -->
        <div style="margin-bottom: 15px;">
          <button
            class="btn"
            :class="activeTab === 'preview' ? 'btn-primary' : 'btn-secondary'"
            @click="activeTab = 'preview'"
            style="margin-right: 10px;"
          >
            Просмотр
          </button>
          <button
            class="btn"
            :class="activeTab === 'info' ? 'btn-primary' : 'btn-secondary'"
            @click="activeTab = 'info'"
          >
            Информация
          </button>
        </div>

        <!-- Preview Tab -->
        <div v-if="activeTab === 'preview'">
          <template v-if="report.htmlUrl">
            <iframe :src="`/api/reports/${report.id}/html`" class="preview-frame" frameborder="0"></iframe>
          </template>
          <template v-else-if="report.pdfUrl">
            <embed :src="`http://localhost:8080/reports/${report.id}/pdf`" type="application/pdf" class="preview-frame" />
          </template>

          <div v-else style="text-align: center; padding: 40px; color: #999;">
            Нет доступного контента для предпросмотра
          </div>
        </div>

        <!-- Info Tab -->
        <div v-if="activeTab === 'info'">
          <div class="form-group">
            <label>Название</label>
            <input type="text" v-model="editForm.title" />
          </div>
          <div class="form-group">
            <label>Охранник</label>
            <input type="text" v-model="editForm.guardName" />
          </div>
          <div class="form-group">
            <label>Дата обхода</label>
            <input type="datetime-local" v-model="editForm.date" />
          </div>
          <div class="form-group">
            <label>Примечания</label>
            <textarea v-model="editForm.notes" rows="4"></textarea>
          </div>
          <div class="form-group" v-if="report.filePath">
            <label>Файл</label>
            <div style="color: #666;">{{ report.filePath }} ({{ formatSize(report.size) }})</div>
          </div>
          <div class="form-group" v-if="report.uploadedAt">
            <label>Загружен</label>
            <div style="color: #666;">{{ formatDate(report.uploadedAt) }}</div>
          </div>
        </div>
      </div>

      <!-- Footer -->
      <div class="modal-footer">
        <button class="btn btn-secondary" @click="$emit('close')">Закрыть</button>
        <button class="btn btn-success" @click="handleDownload">⬇ Скачать</button>
        <button class="btn btn-primary" @click="handleEmail">📧 На email</button>
        <button class="btn btn-secondary" @click="handlePrint">🖨 Печать</button>
        <button class="btn btn-danger" @click="handleDelete">❌ Удалить</button>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import http from '../api/http'

export default {
  name: 'ReportPreviewModal',
  props: {
    report: {
      type: Object,
      required: true
    }
  },
  emits: ['close', 'delete'],
  setup(props, { emit }) {
    const activeTab = ref('preview')
    const editForm = reactive({
      title: '',
      guardName: '',
      date: '',
      notes: ''
    })

    const formatDate = (dateStr) => {
      if (!dateStr) return '—'
      return new Date(dateStr).toLocaleString('ru-RU', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      })
    }

    const formatSize = (size) => {
      if (!size) return '—'
      if (size < 1024) return size + ' Б'
      if (size < 1024 * 1024) return (size / 1024).toFixed(1) + ' КБ'
      return (size / (1024 * 1024)).toFixed(1) + ' МБ'
    }

    const saveChanges = async () => {
      try {
        await http.put(`/api/reports/${props.report.id}`, editForm)
        alert('Изменения сохранены')
      } catch (error) {
        alert('Ошибка при сохранении')
      }
    }

    const handleDownload = () => {
      window.open(`/api/reports/${props.report.id}/download`, '_blank')
    }

    const handleEmail = () => {
      emit('openEmail', props.report.id)
    }

    const handlePrint = () => {
      if (props.report.htmlUrl) {
        window.open(`/api/reports/${props.report.id}/html`, '_blank')
      } else if (props.report.pdfUrl) {
        window.open(`/api/reports/${props.report.id}/pdf`, '_blank')
      }
    }

    const handleDelete = () => {
      if (!confirm('Удалить этот отчёт?')) return
      emit('delete', props.report.id)
    }

    onMounted(() => {
      editForm.title = props.report.title || ''
      editForm.guardName = props.report.guardName || ''
      editForm.notes = props.report.notes || ''
      if (props.report.date) {
        editForm.date = new Date(props.report.date).toISOString().slice(0, 16)
      }
    })

    return {
      activeTab,
      editForm,
      formatDate,
      formatSize,
      saveChanges,
      handleDownload,
      handleEmail,
      handlePrint,
      handleDelete
    }
  }
}
</script>
