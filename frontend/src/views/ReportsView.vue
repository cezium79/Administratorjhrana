<template>
  <div>
    <div class="header">
      <h1>🛡️ Контроль обходов</h1>
      <div class="header-actions">
        <span>Администратор</span>
        <button class="btn btn-secondary btn-small" @click="handleLogout">Выйти</button>
      </div>
    </div>

    <div class="container">
      <!-- Filters -->
      <div class="filters">
        <div class="form-group">
          <label>Охранник</label>
          <select v-model="filters.guardName" class="input" style="width: 100%">
            <option value="">Все охранники</option>
            <option v-for="name in guardNames" :key="name" :value="name">{{ name }}</option>
          </select>
        </div>
        <div class="form-group">
          <label>Поиск</label>
          <input
            type="text"
            v-model="filters.title"
            placeholder="Поиск по названию"
            class="input"
          />
        </div>
        <div class="form-group">
          <label>Дата с</label>
          <input
            type="date"
            v-model="filters.dateFrom"
            class="input"
          />
        </div>
        <div class="form-group">
          <label>Дата по</label>
          <input
            type="date"
            v-model="filters.dateTo"
            class="input"
          />
        </div>
        <div class="form-group" style="display: flex; align-items: flex-end">
          <button class="btn btn-primary" @click="loadReports">Применить</button>
        </div>
      </div>

      <!-- Reports Table -->
      <div class="table-container">
        <div v-if="loading" class="loading">
          <div class="spinner"></div>
        </div>
        <table v-else>
          <thead>
            <tr>
              <th @click="changeSort('id')">№ ↕</th>
              <th @click="changeSort('uploadedAt')">Дата и время ↕</th>
              <th @click="changeSort('guardName')">Охранник ↕</th>
              <th @click="changeSort('title')">Название ↕</th>
              <th>Тип</th>
              <th>Размер</th>
              <th>Действия</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="reports.length === 0">
              <td colspan="7" style="text-align: center; padding: 40px; color: #999;">
                Отчёты не найдены
              </td>
            </tr>
            <tr v-for="report in reports" :key="report.id" @click="openReport(report)">
              <td>{{ report.id }}</td>
              <td>{{ formatDate(report.uploadedAt) }}</td>
              <td>{{ report.guardName || '—' }}</td>
              <td>{{ report.title || '—' }}</td>
              <td><ReportTypeBadge :report="report" /></td>
              <td>{{ formatSize(report.size) }}</td>
              <td @click.stop>
                <button class="btn btn-primary btn-small" @click="previewReport(report)" title="Просмотр">👁</button>
                <button class="btn btn-success btn-small" @click="downloadReport(report)" title="Скачать">⬇</button>
                <button class="btn btn-secondary btn-small" @click="printReport(report)" title="Печать">🖨</button>
                <button class="btn btn-danger btn-small" @click="deleteReport(report)" title="Удалить">❌</button>
              </td>
            </tr>
          </tbody>
        </table>

        <!-- Pagination -->
        <div class="pagination" v-if="totalPages > 1">
          <button
            class="btn btn-secondary btn-small"
            :disabled="currentPage === 0"
            @click="goToPage(currentPage - 1)"
          >
            ← Назад
          </button>
          <span class="pagination-info">
            Страница {{ currentPage + 1 }} из {{ totalPages }} (всего {{ totalElements }})
          </span>
          <button
            class="btn btn-secondary btn-small"
            :disabled="currentPage >= totalPages - 1"
            @click="goToPage(currentPage + 1)"
          >
            Вперёд →
          </button>
        </div>
      </div>
    </div>

    <!-- Report Preview Modal -->
    <ReportPreviewModal
      v-if="selectedReport"
      :report="selectedReport"
      @close="selectedReport = null"
      @delete="(id) => handleDelete(id)"
      @openEmail="(id) => openEmailModal(id)"
    />

    <!-- Send Email Modal -->
    <div v-if="showEmailModal" class="modal-overlay" @click.self="showEmailModal = false">
      <div class="modal" style="max-width: 400px;">
        <div class="modal-header">
          <h2>Отправить на email</h2>
          <button class="modal-close" @click="showEmailModal = false">×</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>Адрес email</label>
            <input type="email" v-model="emailAddress" placeholder="email@example.com" />
          </div>
          <div v-if="emailError" class="error-message">{{ emailError }}</div>
          <div v-if="emailSuccess" class="success-message">{{ emailSuccess }}</div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-secondary" @click="showEmailModal = false">Отмена</button>
          <button class="btn btn-primary" @click="sendEmail" :disabled="emailSending">
            {{ emailSending ? 'Отправка...' : 'Отправить' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import http from '../api/http'
import ReportTypeBadge from '../components/ReportTypeBadge.vue'
import ReportPreviewModal from '../components/ReportPreviewModal.vue'

export default {
  name: 'ReportsView',
  components: {
    ReportTypeBadge,
    ReportPreviewModal
  },
  setup() {
    const reports = ref([])
    const loading = ref(false)
    const currentPage = ref(0)
    const pageSize = ref(20)
    const totalElements = ref(0)
    const totalPages = ref(0)
    const guardNames = ref([])
    const selectedReport = ref(null)
    const sortField = ref('uploadedAt')
    const sortDirection = ref('DESC')
    const showEmailModal = ref(false)
    const emailReportId = ref(null)
    const emailAddress = ref('')
    const emailError = ref('')
    const emailSuccess = ref('')
    const emailSending = ref(false)

    const filters = ref({
      guardName: '',
      title: '',
      dateFrom: '',
      dateTo: ''
    })

    const formatDate = (dateStr) => {
      if (!dateStr) return '—'
      const d = new Date(dateStr)
      return d.toLocaleString('ru-RU', {
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

    const loadReports = async () => {
      loading.value = true
      try {
        const params = {
          page: currentPage.value,
          size: pageSize.value,
          sortBy: sortField.value,
          direction: sortDirection.value
        }

        if (filters.value.guardName) {
          params.guardName = filters.value.guardName
        }
        if (filters.value.title) {
          params.title = filters.value.title
        }
        if (filters.value.dateFrom) {
          params.dateFrom = new Date(filters.value.dateFrom).toISOString()
        }
        if (filters.value.dateTo) {
          params.dateTo = new Date(filters.value.dateTo + 'T23:59:59').toISOString()
        }

        const response = await http.get('/api/reports', { params })
        reports.value = response.data.content || []
        totalElements.value = response.data.totalElements || 0
        totalPages.value = response.data.totalPages || 0
      } catch (error) {
        console.error('Error loading reports:', error)
      } finally {
        loading.value = false
      }
    }

    const loadGuardNames = async () => {
      try {
        const response = await http.get('/api/reports/filters')
        guardNames.value = response.data.guardNames || []
      } catch (e) {
        // ignore
      }
    }

    const changeSort = (field) => {
      if (sortField.value === field) {
        sortDirection.value = sortDirection.value === 'ASC' ? 'DESC' : 'ASC'
      } else {
        sortField.value = field
        sortDirection.value = 'DESC'
      }
      loadReports()
    }

    const goToPage = (page) => {
      currentPage.value = page
      loadReports()
    }

    const openReport = (report) => {
      selectedReport.value = report
    }

    const previewReport = (report) => {
      selectedReport.value = report
    }

    const downloadReport = async (report) => {
      try {
        const response = await http.get(`/api/reports/${report.id}/download`, {
          responseType: 'blob'
        })
        const url = window.URL.createObjectURL(new Blob([response.data]))
        const link = document.createElement('a')
        link.href = url
        link.setAttribute('download', report.filePath || `report_${report.id}.pdf`)
        document.body.appendChild(link)
        link.click()
        link.remove()
        window.URL.revokeObjectURL(url)
      } catch (error) {
        console.error('Download error:', error)
      }
    }

    const printReport = (report) => {
      if (report.htmlUrl) {
        window.open(`/api/reports/${report.id}/html`, '_blank')
      } else if (report.pdfUrl) {
        window.open(`/api/reports/${report.id}/pdf`, '_blank')
      }
    }

    const deleteReport = async (report) => {
      if (!confirm(`Удалить отчёт "${report.title}"?`)) return
      try {
        await http.delete(`/api/reports/${report.id}`)
        await loadReports()
      } catch (error) {
        console.error('Delete error:', error)
      }
    }

    const handleDelete = async (id) => {
      await loadReports()
      selectedReport.value = null
    }

    const handleLogout = async () => {
      window.location.href = '/logout'
    }

    const openEmailModal = (reportId) => {
      emailReportId.value = reportId
      emailAddress.value = ''
      emailError.value = ''
      emailSuccess.value = ''
      showEmailModal.value = true
    }

    const sendEmail = async () => {
      if (!emailAddress.value) {
        emailError.value = 'Введите адрес email'
        return
      }
      emailSending.value = true
      emailError.value = ''
      emailSuccess.value = ''
      try {
        await http.post(`/api/reports/${emailReportId.value}/email`, {
          email: emailAddress.value
        })
        emailSuccess.value = 'Отчёт успешно отправлен!'
        setTimeout(() => {
          showEmailModal.value = false
        }, 1500)
      } catch (error) {
        emailError.value = 'Ошибка при отправке'
      } finally {
        emailSending.value = false
      }
    }

    onMounted(() => {
      loadReports()
      loadGuardNames()
    })

    return {
      reports,
      loading,
      currentPage,
      totalElements,
      totalPages,
      guardNames,
      selectedReport,
      filters,
      showEmailModal,
      emailAddress,
      emailError,
      emailSuccess,
      emailSending,
      formatDate,
      formatSize,
      loadReports,
      changeSort,
      goToPage,
      openReport,
      previewReport,
      downloadReport,
      printReport,
      deleteReport,
      handleDelete,
      handleLogout,
      openEmailModal,
      sendEmail
    }
  }
}
</script>
