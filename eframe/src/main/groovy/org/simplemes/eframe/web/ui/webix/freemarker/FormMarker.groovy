package org.simplemes.eframe.web.ui.webix.freemarker

import groovy.util.logging.Slf4j
import org.simplemes.eframe.data.FieldDefinitionInterface
import org.simplemes.eframe.web.ui.WidgetFactory


/*
 * Copyright Michael Houston 2018. All rights reserved.
 * Original Author: mph
 *
*/

/**
 * Provides the efForm Freemarker marker implementation.
 * This builds a form for user data entry and provides access to a submit function.
 * This marker can be wrapped around efField and efButton elements.  It can also be used around definition pages
 * (create/edit).
 */
@Slf4j
class FormMarker extends BaseMarker {

  /**
   * The name of the element in the markerContext.markerCoordinator that holds the toolbar javascript.  This is usually
   * the javascript needed for the toolbar element in the form.  It is inserted in a javascript object section,
   * so it should be the format '{view: "toolbar", . . .}'.
   */
  public static final String COORDINATOR_TOOLBAR = 'toolbar'

  /**
   * Executes the directive, with the values passed by the setValues() method.
   */
  @Override
  void execute() {
    def id = parameters.id ?: '_form'
    def divID = "${id}Content"
    markerContext.markerCoordinator.formID = id

    def content = renderContent()

    if (parameters.fieldDefinitions) {
      content = buildFieldsFromDefinitions() + content
    }

    def toolbar = markerContext?.markerCoordinator?.others[COORDINATOR_TOOLBAR] ?: ''
    if (toolbar) {
      toolbar += ',\n'
    }

    def postScript = markerContext?.markerCoordinator?.getPostscript() ?: ''
    def preScript = markerContext?.markerCoordinator?.getPrescript() ?: ''

    def width = parameters.width ?: '90%'

    def dashboard = parameters.dashboard

    if (dashboard) {
      def params = getModelValue('params')
      def variable = params?._variable
      def panel = params?._panel ?: 'A'
      if (!variable) {
        throw new MarkerException("efForm with dashboard option must be passed a panel/variable from the dashboard", this)
      }
      def buttonsHolder = ''
      if (dashboard == 'buttonHolder') {
        def elements = """ elements: [{view: "template", id: "ButtonsContent${panel}" """
        def opts = """type: "clean", borderless: true"""
        buttonsHolder = """ ,{view: "form", id: "Buttons${panel}", $opts, $elements, template: "-"}]}"""
      }
      def res = """
        $preScript
        ${variable}.display = {
          view: 'form', id: '${id}', type: 'clean', margin: 0,
          rows: [
            {height: 10}
            ${content}
            ${buttonsHolder}
            ,{}
          ]
        };
        $postScript

      """
      write(res)
    } else {
      def res = """
        <div id="$divID"></div>
        <script>
          var ${id}FormData = [
          ${content}
          ]; // form
          $preScript
          webix.ui({
            container: '$divID',
            type: "space", margin: 0, padding: 2, cols: [
              { align: "center", body: {
                  rows: [
                    ${toolbar}
                    {view: "form", id: '${id}', scroll: false, width: tk.pw('${width}'), elements: ${id}FormData}
                  ]
                }
              }
            ]
          });
          $postScript
        </script>
      """
      write(res)
    }
  }

  /**
   * Builds fields from the field definitions stored in the model.
   * @return
   */
  String buildFieldsFromDefinitions() {
    def sb = new StringBuilder()
    def fieldDefinitions = getModelValue(parameters.fieldDefinitions)
    def filterValues = getModelValue('reportFilterValues')
    if (!fieldDefinitions) {
      throw new MarkerException("efForm fieldDefinitions (${parameters.fieldDefinitions}) not found in the model", this)
    }

    // Now, sort the list of fields
    def list = []
    for (field in fieldDefinitions) {
      list << unwrap(field)
    }
    list.sort { a, b -> a.sequence <=> b.sequence }

    for (fieldDefinition in list) {
      String name = fieldDefinition.name
      def widgetContext = buildWidgetContext((FieldDefinitionInterface) fieldDefinition)
      widgetContext.object = [:]
      widgetContext.object[name] = filterValues[name]
      widgetContext.parameters.id = name
      addFieldSpecificParameters(widgetContext, (String) name, (Map) parameters)
      def widget = WidgetFactory.instance.build(widgetContext)
      if (sb) {
        sb << ",\n"
      }
      sb << widget.build()
      log.trace('buildFields() field={}, widgetContext={}, widget={}', name, widgetContext, widget)
    }


    return sb.toString()
  }
}
