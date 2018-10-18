data "azurerm_key_vault_secret" "source_test_s2s_secret" {
  name      = "microservicekey-jui-webapp"
  vault_uri = "${local.s2s_vault_url}"
}

resource "azurerm_key_vault_secret" "test_s2s_secret" {
  name      = "cor-s2s-token"
  value     = "${data.azurerm_key_vault_secret.source_test_s2s_secret.value}"
  vault_uri = "${module.local_key_vault.key_vault_uri}"
}
