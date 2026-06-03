/**
 * Hiển thị thông báo Lỗi/Thành công bằng SweetAlert2 Toast.
 * - Tự động đọc data-error-message / data-success-message trên <body> (flash message).
 * - Expose helper window.appToast(icon, message) để dùng cho AJAX/fetch (không reload trang).
 */

(function () {
  let toastInstance = null;

  function getToast() {
    if (toastInstance) return toastInstance;
    if (!window.Swal) return null;

    toastInstance = Swal.mixin({
      toast: true,
      position: 'bottom-end',
      showConfirmButton: false,
      timer: 3500,
      timerProgressBar: true,
      didOpen: (toast) => {
        toast.addEventListener('mouseenter', Swal.stopTimer);
        toast.addEventListener('mouseleave', Swal.resumeTimer);
      }
    });

    return toastInstance;
  }

  // Global helper for inline scripts / fetch handlers
  window.appToast = function (icon, message) {
    const toast = getToast();
    if (toast) {
      toast.fire({ icon, title: message });
    } else {
      alert(message);
    }
  };

  document.addEventListener("DOMContentLoaded", function () {
    const bodyElement = document.body;

    const errorMessage = bodyElement.dataset.errorMessage;
    if (errorMessage) {
      window.appToast('error', errorMessage);
    }

    const successMessage = bodyElement.dataset.successMessage;
    if (successMessage) {
      window.appToast('success', successMessage);
    }
  });
})();