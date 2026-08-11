<template>
  <span class="badge" :class="badgeClass">
    {{ badgeText }}
  </span>
</template>

<script>
export default {
  name: 'ReportTypeBadge',
  props: {
    report: {
      type: Object,
      required: true
    }
  },
  computed: {
    badgeText() {
      const hasHtml = this.report.htmlUrl || this.report.htmlContent
      const hasPdf = this.report.pdfUrl
      if (hasHtml && hasPdf) return 'HTML+PDF'
      if (hasPdf) return 'PDF'
      if (hasHtml) return 'HTML'
      return 'Файл'
    },
    badgeClass() {
      const hasHtml = this.report.htmlUrl || this.report.htmlContent
      const hasPdf = this.report.pdfUrl
      if (hasHtml && hasPdf) return 'badge-both'
      if (hasPdf) return 'badge-pdf'
      if (hasHtml) return 'badge-html'
      return 'badge-html'
    }
  }
}
</script>
