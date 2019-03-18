locals {
  sscsVaultName = "sscs-${local.local_env}"
}

data "azurerm_key_vault" "sscs_key_vault" {
  name                = "${local.sscsVaultName}"
  resource_group_name = "${local.sscsVaultName}"
}

data "azurerm_key_vault_secret" "idam-sscs-oauth2-client-secret" {
  name      = "idam-sscs-oauth2-client-secret"
  vault_uri = "${data.azurerm_key_vault.sscs_key_vault.vault_uri}"
}

resource "azurerm_key_vault_secret" "idam-sscs-secret" {
  name = "idam-sscs-oauth2-client-secret"
  value = "${data.azurerm_key_vault_secret.idam-sscs-oauth2-client-secret.value}"
  vault_uri = "${module.local_key_vault.key_vault_uri}"
}
