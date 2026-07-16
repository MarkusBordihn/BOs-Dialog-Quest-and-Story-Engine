/*
 * Copyright 2025 Markus Bordihn
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package de.markusbordihn.dialogqueststoryengine.data.issue;

public enum IssueCode {
  CONTENT_PARSE_FAILED(IssueSeverity.ERROR, "Content file could not be parsed safely"),
  DUPLICATE_ID(IssueSeverity.WARNING, "Duplicate content ID, previous entry overwritten"),
  DUPLICATE_NODE_ID(IssueSeverity.ERROR, "Duplicate dialog node id within the same file"),
  EMPTY_CHOICES(IssueSeverity.WARNING, "Dialog node has no choices (terminal node)"),
  EMPTY_PAGES(IssueSeverity.ERROR, "Story entry must have at least one page"),
  EMPTY_STEPS(IssueSeverity.WARNING, "Quest has no steps defined"),
  INVALID_BLOCK_POS(
      IssueSeverity.ERROR, "Block position object is missing x/y/z or has non-integer values"),
  INVALID_FIELD_TYPE(IssueSeverity.ERROR, "Field has an unexpected type"),
  INVALID_ID(IssueSeverity.ERROR, "Content ID is invalid"),
  INVALID_RESOURCE_LOCATION(IssueSeverity.ERROR, "Value is not a valid ResourceLocation"),
  INVALID_UUID(IssueSeverity.ERROR, "Value is not a valid UUID"),
  JSON_PARSE_FAILED(IssueSeverity.ERROR, "JSON could not be parsed"),
  MISSING_DIALOG_NODE(
      IssueSeverity.ERROR, "Choice next field references a non-existent dialog node"),
  MISSING_FIELD(IssueSeverity.ERROR, "Required field is missing"),
  MISSING_SCHEMA(IssueSeverity.ERROR, "Missing required 'schema' field"),
  MISSING_START_NODE(
      IssueSeverity.ERROR, "Dialog start_node references a node that does not exist"),
  MISSING_THEME_REFERENCE(
      IssueSeverity.WARNING, "Referenced theme does not exist in any loaded registry"),
  CIRCULAR_DIALOG_FLOW(
      IssueSeverity.WARNING, "Dialog graph contains a cycle reachable from start_node"),
  UNREACHABLE_DIALOG_NODE(IssueSeverity.WARNING, "Dialog node has no path from start_node"),
  UNKNOWN_BINDING_KIND(IssueSeverity.ERROR, "Unknown interaction binding kind"),
  UNKNOWN_INTERACTION_EVENT(IssueSeverity.ERROR, "Unknown interaction event type"),
  COMMAND_ACTION_DISABLED(
      IssueSeverity.WARNING, "run_command action is disabled by server config - action skipped"),
  UNKNOWN_ACTION_TYPE(IssueSeverity.ERROR, "Action type is not registered - action skipped"),
  AMBIGUOUS_ACTION_TYPE(
      IssueSeverity.ERROR,
      "Action shorthand matches more than one action type - use explicit type"),
  CHOICE_NEXT_CLOSE_CONFLICT(
      IssueSeverity.ERROR, "Dialog choice 'next' and 'close' are mutually exclusive"),
  UNKNOWN_CONDITION_TYPE(
      IssueSeverity.ERROR, "Condition type is not registered - evaluates to false"),
  UNKNOWN_STEP_TYPE(
      IssueSeverity.WARNING, "Quest step type is not registered - step only progresses manually"),
  UNKNOWN_STEP_PREREQUISITE(
      IssueSeverity.ERROR, "Step requires references a step that does not exist"),
  SELF_STEP_PREREQUISITE(IssueSeverity.ERROR, "Step requires itself"),
  DUPLICATE_STEP_PREREQUISITE(
      IssueSeverity.ERROR, "Step requires lists the same step more than once"),
  STEP_PREREQUISITE_CYCLE(IssueSeverity.ERROR, "Step prerequisites form a dependency cycle"),
  UNKNOWN_QUEST_PREREQUISITE(
      IssueSeverity.ERROR, "Quest prerequisite references a quest that does not exist"),
  SELF_QUEST_PREREQUISITE(IssueSeverity.ERROR, "Quest lists itself as a prerequisite"),
  DUPLICATE_QUEST_PREREQUISITE(
      IssueSeverity.ERROR, "Quest prerequisites list the same quest more than once"),
  QUEST_PREREQUISITE_CYCLE(IssueSeverity.ERROR, "Quest prerequisites form a dependency cycle"),
  UNKNOWN_REWARD_TYPE(IssueSeverity.ERROR, "Reward entry type is not a known reward type"),
  INVALID_REWARD_AMOUNT(IssueSeverity.ERROR, "Reward amount or count must be a positive integer"),
  LOCALIZED_TEXT_CONFLICT(
      IssueSeverity.ERROR, "Literal text and localization key are mutually exclusive"),
  LOCALIZED_ARGS_WITHOUT_KEY(
      IssueSeverity.ERROR, "Text arguments require the localization key form"),
  UNKNOWN_CONTEXT_PROVIDER(
      IssueSeverity.ERROR, "Text argument provider type is not a registered ContextValueProvider"),
  INVALID_CONTEXT_ARGUMENT(IssueSeverity.ERROR, "Text argument is not valid for its provider"),
  UNSUPPORTED_STEP_TYPE(
      IssueSeverity.WARNING, "Quest step type is reserved but not supported in this version"),
  UNKNOWN_STORY_TYPE(IssueSeverity.ERROR, "Unknown story entry type"),
  UNKNOWN_THEME_LAYOUT(IssueSeverity.ERROR, "Unknown theme layout"),
  UNSUPPORTED_SCHEMA(IssueSeverity.ERROR, "Schema version is not supported"),
  CONDITION_FACT_TYPE_MISMATCH(
      IssueSeverity.WARNING, "Condition value type does not match expected fact value type");

  private final IssueSeverity defaultSeverity;
  private final String description;

  IssueCode(IssueSeverity defaultSeverity, String description) {
    this.defaultSeverity = defaultSeverity;
    this.description = description;
  }

  public IssueSeverity defaultSeverity() {
    return this.defaultSeverity;
  }

  public String description() {
    return this.description;
  }
}
