<#assign title><@efTitle type='edit'/></#assign>

<#include "../includes/header.ftl" />
<#include "../includes/definition.ftl" />

<@efForm id="edit">
    <@efEdit fields@label="" fields@sequence@default="tk.findMaxGridValue(gridName, 'sequence')+10"/>
</@efForm>

<#include "../includes/footer.ftl" />

