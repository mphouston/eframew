/*
 * Copyright (c) Michael Houston 2020. All rights reserved.
 */

package org.simplemes.eframe.test.page

import geb.Module

/**
 * Defines the GEB page elements for a standard editable text field.  This contains these elements:
 * <p>
 * <h4>Example Page Definition:</h4>
 * <pre>
 *   static content = &#123;
 *     order &#123; module(new TextFieldModule(field: 'order')) &#125;
 *   &#125;
 * </pre>
 *
 * <p>
 * <h4>Example Test Spec Usage:</h4>
 * <pre>
 *   order.input.value('M1001')
 *   order.input.value == 'M1001'
 * </pre>
 *
 * <h4>This contains these elements:</h4>
 * <ul>
 *   <li><b>label</b> - The label text.</li>
 *   <li><b>input</b> - The input field itself.</li>
 *   <li><b>invalid</b> - True if the input field is marked as invalid (css makes it appear red).</li>
 * </ul>
 *
 */
@SuppressWarnings(["GroovyAssignabilityCheck", "GroovyUnusedDeclaration"])
class TextFieldModule extends Module {
  String field

  static content = {
    label { sanitize($('label', for: field).text()) }
    input { $("input#${field}") }
  }

  /**
   * Sanitizes non-printable characters from the HTML text.
   * @param html The HTML.
   * @return The sanitized text.
   */
  String sanitize(String html) {
    return html?.replaceAll('\n', '')
  }

}
