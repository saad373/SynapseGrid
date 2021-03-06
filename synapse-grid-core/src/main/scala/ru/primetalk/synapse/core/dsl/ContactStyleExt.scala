package ru.primetalk.synapse.core.dsl

import scala.collection.immutable.Iterable

/**
 * An extension to add style information to contacts. It is used primarily by
 * the SystemRenderer to convert StaticSystem to .dot-file
 * @author zhizhelev, 05.04.15.
 */
trait ContactStyleExt extends SystemBuilderApi {
  trait ContactStyle

  /** Default style for contact.*/
  case object NormalContact extends ContactStyle
//
//  case object StateContact extends ContactStyle

  implicit val ContactStyleExtId = new SystemBuilderExtensionId[ContactStyleExtension](new ContactStyleExtension(_))

  implicit object ContactStyleStaticExtId extends StaticSystemExtensionId[ContactStyleStaticExtension]

  class ContactStyleExtension(val sb: SystemBuilder) extends SystemBuilderExtension{
    private[ContactStyleExt]
    var styles = List[(Contact[_], ContactStyle)]()

    /** Opportunity for extension to hook into method
      * SystemBuilder#toStaticSystem".
      * It can also add some information to extensions map. */
    override def postProcess(s: StaticSystem): StaticSystem =
      s.extend(new ContactStyleStaticExtension(styles.toMap.withDefaultValue(NormalContact)))(ContactStyleStaticExtId)

  }

  case class ContactStyleStaticExtension(styles:Map[Contact[_], ContactStyle]) {
    lazy val reversed = styles.groupBy(_._2).map(grp => (grp._1, grp._2.map(_._1)))
    def style(c:Contact[_]):ContactStyle =
      styles.getOrElse(c, NormalContact)

    def styledWith(s:ContactStyle): Iterable[Contact[_]] = reversed.getOrElse(s,Iterable.empty)

  }

  implicit class StyleableContact[T](c:Contact[T]){
    def styled(s:ContactStyle)(implicit sb:SystemBuilder) = {
      val ext = sb.extend(ContactStyleExtId)
      ext.styles = (c,s) :: ext.styles
      c
    }
  }
//  implicit class StyledSystem(s:StaticSystem){
//    def styles:Map[Contact[_], ContactStyle] = s.extensionOpt[ContactStyleStaticExtension].
//      map(_.styles).getOrElse(Map())
//  }
}
