package dev.threadly.runtime.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * ConditionEvaluator evaluates conditions for branching logic.
 * Supports comparison operators, logical operators, and variable resolution.
 */
@Service
@Slf4j
public class ConditionEvaluator {

  /**
   * Evaluate a condition expression
   *
   * @param conditionDef The condition definition JSON node
   * @param variables Session variables for evaluation
   * @return true if condition is met, false otherwise
   */
  public boolean evaluate(JsonNode conditionDef, Map<String, Object> variables) {
    if (conditionDef == null) {
      return true;
    }

    // Single operator condition
    if (conditionDef.has("operator")) {
      return evaluateSingleCondition(conditionDef, variables);
    }

    // Logical AND condition (all must be true)
    if (conditionDef.has("and")) {
      JsonNode andNode = conditionDef.get("and");
      if (andNode.isArray()) {
        for (JsonNode condition : andNode) {
          if (!evaluate(condition, variables)) {
            return false;
          }
        }
        return true;
      }
    }

    // Logical OR condition (any can be true)
    if (conditionDef.has("or")) {
      JsonNode orNode = conditionDef.get("or");
      if (orNode.isArray()) {
        for (JsonNode condition : orNode) {
          if (evaluate(condition, variables)) {
            return true;
          }
        }
        return false;
      }
    }

    // Logical NOT condition
    if (conditionDef.has("not")) {
      return !evaluate(conditionDef.get("not"), variables);
    }

    // Default to true if no condition
    return true;
  }

  /**
   * Evaluate a single condition with operator
   */
  private boolean evaluateSingleCondition(JsonNode conditionDef, Map<String, Object> variables) {
    String operator = conditionDef.get("operator").asText();
    String variable = conditionDef.has("variable") ? conditionDef.get("variable").asText() : null;
    Object expectedValue = conditionDef.has("value") ? conditionDef.get("value") : null;

    if (variable == null) {
      log.warn("Condition missing variable field");
      return false;
    }

    Object actualValue = variables.get(variable);

    return evaluateOperator(operator, actualValue, expectedValue);
  }

  /**
   * Evaluate operator logic
   */
  private boolean evaluateOperator(String operator, Object actualValue, Object expectedValue) {
    switch (operator.toUpperCase()) {
      case "EQUALS":
      case "EQ":
      case "==":
        return compareEqual(actualValue, expectedValue);

      case "NOT_EQUALS":
      case "NEQ":
      case "!=":
        return !compareEqual(actualValue, expectedValue);

      case "GREATER_THAN":
      case "GT":
      case ">":
        return compareGreater(actualValue, expectedValue);

      case "LESS_THAN":
      case "LT":
      case "<":
        return compareLess(actualValue, expectedValue);

      case "GREATER_EQUALS":
      case "GTE":
      case ">=":
        return compareGreaterOrEqual(actualValue, expectedValue);

      case "LESS_EQUALS":
      case "LTE":
      case "<=":
        return compareLessOrEqual(actualValue, expectedValue);

      case "CONTAINS":
        return compareContains(actualValue, expectedValue);

      case "NOT_CONTAINS":
        return !compareContains(actualValue, expectedValue);

      case "STARTS_WITH":
        return compareStartsWith(actualValue, expectedValue);

      case "ENDS_WITH":
        return compareEndsWith(actualValue, expectedValue);

      case "IN":
        return compareIn(actualValue, expectedValue);

      case "EXISTS":
        return actualValue != null;

      case "NOT_EXISTS":
        return actualValue == null;

      case "IS_EMPTY":
        return isEmpty(actualValue);

      case "IS_NOT_EMPTY":
        return !isEmpty(actualValue);

      default:
        log.warn("Unknown operator: {}", operator);
        return false;
    }
  }

  /**
   * Compare values for equality
   */
  private boolean compareEqual(Object actual, Object expected) {
    if (actual == null && expected == null) {
      return true;
    }
    if (actual == null || expected == null) {
      return false;
    }
    return actual.toString().equals(expected.toString());
  }

  /**
   * Compare for greater than
   */
  private boolean compareGreater(Object actual, Object expected) {
    try {
      double actualNum = getNumericValue(actual);
      double expectedNum = getNumericValue(expected);
      return actualNum > expectedNum;
    } catch (NumberFormatException e) {
      log.warn("Cannot compare non-numeric values: {} > {}", actual, expected);
      return false;
    }
  }

  /**
   * Compare for less than
   */
  private boolean compareLess(Object actual, Object expected) {
    try {
      double actualNum = getNumericValue(actual);
      double expectedNum = getNumericValue(expected);
      return actualNum < expectedNum;
    } catch (NumberFormatException e) {
      log.warn("Cannot compare non-numeric values: {} < {}", actual, expected);
      return false;
    }
  }

  /**
   * Compare for greater or equal
   */
  private boolean compareGreaterOrEqual(Object actual, Object expected) {
    try {
      double actualNum = getNumericValue(actual);
      double expectedNum = getNumericValue(expected);
      return actualNum >= expectedNum;
    } catch (NumberFormatException e) {
      log.warn("Cannot compare non-numeric values: {} >= {}", actual, expected);
      return false;
    }
  }

  /**
   * Compare for less or equal
   */
  private boolean compareLessOrEqual(Object actual, Object expected) {
    try {
      double actualNum = getNumericValue(actual);
      double expectedNum = getNumericValue(expected);
      return actualNum <= expectedNum;
    } catch (NumberFormatException e) {
      log.warn("Cannot compare non-numeric values: {} <= {}", actual, expected);
      return false;
    }
  }

  /**
   * Compare for string contains
   */
  private boolean compareContains(Object actual, Object expected) {
    if (actual == null) {
      return false;
    }
    return actual.toString().contains(expected != null ? expected.toString() : "");
  }

  /**
   * Compare for string starts with
   */
  private boolean compareStartsWith(Object actual, Object expected) {
    if (actual == null) {
      return false;
    }
    return actual.toString().startsWith(expected != null ? expected.toString() : "");
  }

  /**
   * Compare for string ends with
   */
  private boolean compareEndsWith(Object actual, Object expected) {
    if (actual == null) {
      return false;
    }
    return actual.toString().endsWith(expected != null ? expected.toString() : "");
  }

  /**
   * Compare value in list
   */
  private boolean compareIn(Object actual, Object expectedList) {
    if (actual == null) {
      return false;
    }
    // TODO: Implement list membership check
    return false;
  }

  /**
   * Check if value is empty
   */
  private boolean isEmpty(Object value) {
    if (value == null) {
      return true;
    }
    String strValue = value.toString();
    return strValue.isEmpty() || strValue.equals("null");
  }

  /**
   * Get numeric value from object
   */
  private double getNumericValue(Object value) throws NumberFormatException {
    if (value instanceof Number) {
      return ((Number) value).doubleValue();
    }
    return Double.parseDouble(value.toString());
  }
}
