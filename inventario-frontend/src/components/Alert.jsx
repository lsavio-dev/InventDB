export default function Alert({ tipo = "info", children, onClose }) {
  if (!children) return null;
  return (
    <div className={`alert alert-${tipo}`}>
      <span>{children}</span>
      {onClose && (
        <button className="alert-close" onClick={onClose} aria-label="Fechar">
          ×
        </button>
      )}
    </div>
  );
}
