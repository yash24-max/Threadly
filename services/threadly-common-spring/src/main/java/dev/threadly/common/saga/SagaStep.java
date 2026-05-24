package dev.threadly.common.saga;

/**
 * Interface for a single step in a saga.
 *
 * A saga step is a unit of work that can be executed and compensated (rolled back).
 * Steps are executed in order, and if any step fails, all previous steps are compensated.
 *
 * Implementation requirements:
 * 1. {@link #execute(String, int)} performs the actual work (call external service, update DB, etc.)
 * 2. {@link #compensate(String, int)} reverses the effect of execute()
 * 3. Both methods should be idempotent: calling them multiple times with the same (sagaId, stepIndex)
 *    should result in the same state
 *
 * Example:
 * <pre>
 * {@code
 * public class CheckBillingStep implements SagaStep {
 *   @Autowired private BillingServiceClient billingClient;
 *   @Autowired private SagaStepRepository stepRepository;
 *
 *   @Override
 *   public void execute(String sagaId, int stepIndex) {
 *     String key = sagaId + "#" + stepIndex;
 *     if (stepRepository.existsByKey(key)) {
 *       return; // Already executed, skip
 *     }
 *
 *     BillingCheckResponse result = billingClient.canConverse(conversationId);
 *     if (!result.isAllowed()) {
 *       throw new BillingException("User not allowed to converse");
 *     }
 *
 *     stepRepository.save(new SagaStepRecord(key, "billing.checked"));
 *   }
 *
 *   @Override
 *   public void compensate(String sagaId, int stepIndex) {
 *     // No compensation needed - just a check, didn't modify state
 *   }
 * }
 * }
 * </pre>
 */
public interface SagaStep {

  /**
   * Execute the step (forward direction).
   *
   * @param sagaId Unique saga identifier
   * @param stepIndex Zero-based step index in the saga
   * @throws Exception if the step fails (saga will rollback)
   */
  void execute(String sagaId, int stepIndex) throws Exception;

  /**
   * Compensate the step (rollback/undo).
   *
   * @param sagaId Unique saga identifier
   * @param stepIndex Zero-based step index in the saga
   * @throws Exception if compensation fails
   */
  void compensate(String sagaId, int stepIndex) throws Exception;
}
