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
  DUPLICATE_ID(IssueSeverity.WARNING, "Duplicate content ID, previous entry overwritten"),
  EMPTY_PAGES(IssueSeverity.ERROR, "Story entry must have at least one page"),
  INVALID_FIELD_TYPE(IssueSeverity.ERROR, "Field has an unexpected type"),
  INVALID_ID(IssueSeverity.ERROR, "Content ID is invalid"),
  INVALID_RESOURCE_LOCATION(IssueSeverity.ERROR, "Value is not a valid ResourceLocation"),
  JSON_PARSE_FAILED(IssueSeverity.ERROR, "JSON could not be parsed"),
  MISSING_FIELD(IssueSeverity.ERROR, "Required field is missing"),
  MISSING_SCHEMA(IssueSeverity.ERROR, "Missing required 'schema' field"),
  MISSING_THEME_REFERENCE(
      IssueSeverity.WARNING, "Referenced theme does not exist in any loaded registry"),
  UNKNOWN_STORY_TYPE(IssueSeverity.ERROR, "Unknown story entry type"),
  UNKNOWN_THEME_LAYOUT(IssueSeverity.ERROR, "Unknown theme layout"),
  UNSUPPORTED_SCHEMA(IssueSeverity.ERROR, "Schema version is not supported");

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
