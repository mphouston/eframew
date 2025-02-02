/*
 * Copyright (c) Michael Houston 2020. All rights reserved.
 */

package sample.domain

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import io.micronaut.data.annotation.AutoPopulated
import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import io.micronaut.data.annotation.MappedProperty
import io.micronaut.data.model.DataType
import org.simplemes.eframe.custom.domain.FlexType
import org.simplemes.eframe.data.annotation.ExtensibleFieldHolder
import org.simplemes.eframe.domain.annotation.DomainEntity

import io.micronaut.core.annotation.Nullable
import javax.persistence.ManyToOne

/**
 * A sample domain class that simulates an custom order component record.
 * Used only in custom field tests (via SampleAddition).
 * Fields include: order, sequence, qty, product(string), notes
 */
@DomainEntity
@MappedEntity()
@ToString(includeNames = true, excludes = ['order'])
@EqualsAndHashCode(includes = ['uuid'])
@SuppressWarnings("unused")
class CustomOrderComponent {
  @ManyToOne
  @MappedProperty(type = DataType.UUID)
  Order order

  Integer sequence = 1

  BigDecimal qty = 1.0
  @Nullable String product
  @Nullable String notes

  @Nullable
  @ManyToOne(targetEntity = AllFieldsDomain)
  @MappedProperty(type = DataType.UUID)
  AllFieldsDomain foreignReference

  @Nullable
  @ManyToOne(targetEntity = FlexType)
  @MappedProperty(type = DataType.UUID)
  FlexType assyDataType

  @Nullable
  @ExtensibleFieldHolder
  @MappedProperty(type = DataType.JSON)
  String customFields


  @Id @AutoPopulated
  @MappedProperty(type = DataType.UUID)
  UUID uuid

  static fieldOrder = ['sequence', 'product', 'qty', 'notes']
  static keys = ['order', 'sequence']

  CustomOrderComponent() {
  }

}