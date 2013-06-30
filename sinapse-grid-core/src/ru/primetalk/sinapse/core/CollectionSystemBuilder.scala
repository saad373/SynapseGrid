///////////////////////////////////////////////////////////////
// © ООО «Праймтолк», 2011-2013                              //
// Все права принадлежат компании ООО «Праймтолк».           //
///////////////////////////////////////////////////////////////
/**
 * ${PROJECT_NAME}
 * © Primetalk Ltd., 2013.
 * All rights reserved.
 * Authors: A.Zhizhelev, A.Nehaev, P. Popov
 * (2-clause BSD license) See LICENSE
 *
 * Created: 28.06.13, zhizhelev
 */
package ru.primetalk.sinapse.core

trait CollectionSystemBuilder extends SystemBuilder {
	/**
	 * Collects all inputs until some other contact not issue control
	 * command to pass through all collected data.
	 */
	def collector[T, TTrigger](name: String, trigger: Contact[TTrigger]): (Contact[T], Contact[List[T]]) = {
		val in = contact[T](name)
		val seqOut = contact[List[T]](name + "List")
		val collection = state[List[T]](name, Nil)
    new ImplStateLinkBuilder(trigger -> seqOut).stateMap(
			(s: List[T], input: TTrigger) ⇒ {
				val s2 = Nil: List[T]
				(s2, s)
			}, collection, "trigger")

		(in, seqOut)
	}
	implicit class CollectingContact[T](c:Contact[T]){
		/** Creates intermediate state and collects all incoming signals in reversed order until some data appear on the trigger.
		 *  Then clears the state and returns collected data in a single list.*/
		def collectReversedUntil[TTrigger](trigger: Contact[TTrigger], name:String = c.name+"ReversedCollector"):Contact[List[T]] = {
			val collection = state[List[T]](name+"State", Nil)
			c.prependList(collection, name+".collectReversed")
			val seqOut = trigger.getState(collection, name+".read")
			trigger.delay(1).clearList(collection, name+".clear")
			seqOut
		}

		/** Creates intermediate state and collects all incoming signals in direct order until some data appear on the trigger.
		 *  Then clears the state and returns collected data in a single list.*/
		def collectUntil[TTrigger](trigger: Contact[TTrigger], name:String = c.name+"Collector"):Contact[List[T]] = {
			val collection = state[List[T]](name+"State", Nil)
			c.prependList(collection, name+".collect")
			val seqOut = contact[List[T]](name+".directOrder")
			(trigger.getState(collection, name+".read") -> seqOut).labelNext("reverseStack").map(_.reverse)
			trigger.delay(1).clearList(collection, name+".clear")
			seqOut
		}
	}
}