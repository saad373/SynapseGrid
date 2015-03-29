///////////////////////////////////////////////////////////////
// © ООО «Праймтолк», 2011-2013                              //
// Все права принадлежат компании ООО «Праймтолк».           //
///////////////////////////////////////////////////////////////
/**
 * SynapseGrid
 * © Primetalk Ltd., 2013.
 * All rights reserved.
 * Authors: A.Zhizhelev, A.Nehaev, P. Popov
 * (2-clause BSD license) See LICENSE
 *
 * Created: 30.06.13, zhizhelev
 */
package ru.primetalk.synapse

import ru.primetalk.synapse.core.impl._

import scala.language.implicitConversions

package object core
  extends SystemBuilderImplicitsApi
  with StaticSystemApi
  with SignalProcessorApi
  with TrellisProducerApi
  with ComponentNavigationApi
  with ExceptionHandlingApi
  with SignalsApi
  with BasicSystemBuilderApi
  with DotUtilsApi
  with ContactsApi
  with EncapsulationApi
  with AuxNumberingExt
  with NextLabelExt
