<#assign title><@efTitle type='edit'/></#assign>

<#include "../includes/header.ftl" />
<#include "../includes/definition.ftl" />

<@efForm id="edit">
    <@efEdit/>
    <@efField field="User.password" id="_pwNew" label="password.label" width="20" required="true" type="password-no-auto"
    after="passwordExpired"/>
    <@efField field="User.password" id="_pwConfirm" label="confirmPassword.label" width="20" required="true"
    type="password-no-auto"  after="_pwNew"/>
</@efForm>

<#include "../includes/footer.ftl" />

