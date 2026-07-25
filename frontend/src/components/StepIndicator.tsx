import "./StepIndicator.css";

const steps = ["결제", "배송지 입력", "출고", "배송완료", "거래확정"];

interface StepIndicatorProps {
  current: number; // 1~5
  labels?: string[];
}

function StepIndicator({ current, labels = steps }: StepIndicatorProps) {
  return (
    <div className="step-indicator">
      {labels.map((label, index) => {
        const step = index + 1;
        const state =
          step < current ? "done" : step === current ? "active" : "todo";
        return (
          <div className="step-item" key={label}>
            <div className="step-row">
              {index > 0 && (
                <div className={`step-line ${step <= current ? "done" : ""}`} />
              )}
              <div className={`step-circle ${state}`}>
                {state === "done" ? "✓" : step}
              </div>
              {index < labels.length - 1 && (
                <div className={`step-line ${step < current ? "done" : ""}`} />
              )}
            </div>
            <span className={`step-label ${state}`}>{label}</span>
          </div>
        );
      })}
    </div>
  );
}

export default StepIndicator;