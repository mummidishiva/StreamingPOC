resource "azurerm_resource_group" "dev_kafka" {
  name     = "rg-kafka-dev"
  location = " centralus"
}

resource "azurerm_storage_account" "dev" {
  name                     = "kakfastorageacc"
  resource_group_name      = azurerm_resource_group.example.name
  location                 = azurerm_resource_group.dev_kafka.location
  account_tier             = "Standard"
  account_replication_type = "LRS"
  account_kind             = "StorageV2"
  is_hns_enabled           = "true"
}

resource "azurerm_storage_data_lake_gen2_filesystem" "dev_kafka" {
  name               = "dev_kafka"
  storage_account_id = azurerm_storage_account.dev_kafka.id
  
}
