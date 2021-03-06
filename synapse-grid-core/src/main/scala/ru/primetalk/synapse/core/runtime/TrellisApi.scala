package ru.primetalk.synapse.core.runtime

import ru.primetalk.synapse.core.components.SignalsApi

/** Basic definitions for trellis processing*/
trait TrellisApi extends SignalsApi {//with RuntimeSystemApi {

  /** The simplest signal processor. Corresponds to FlatMap. */
  type SimpleSignalProcessor = Signal[_] => SignalCollection[Signal[_]]

  /** The context for system is a map from state handles to values. */
  type Context = Map[Contact[_], _]

  /**
   * A snapshot of a running system at some discrete time moment.
   */
  type TrellisElement = (Context, SignalCollection[Signal[_]])

//  type ContextUpdater = List[(Contact[_], _)]
//
//  type TrellisElementUpdater = (ContextUpdater, List[Signal[_]])
//
//  def updateTrellisElement(te: TrellisElement, upd: TrellisElementUpdater): TrellisElement =
//    ((te._1 /: upd._1.reverse)((ctx, u) => ctx + u), upd._2)

  /** A function that makes single step (or a few steps) over time. */
  type TrellisProducer = TrellisElement => TrellisElement
  /** A function that takes a single signal on input and returns the last trellis element.
    * This producer does not store managed state in it.*/
  type TotalTrellisProducer = ((Context, Signal[_]) => TrellisElement)

  implicit class RichTotalTrellisProducer(ttp: TotalTrellisProducer) {
    /** Creates hidden state that will be maintained between different signals.
      * The resulting SimpleSignalProcessor is not thread safe!
      * @param s0 initial state for the first signal. For further signals the internal state is updated automatically.
      * */
    def toSimpleSignalProcessor(s0: Context): SimpleSignalProcessor = {
      @volatile
      var state: Map[Contact[_], _] = s0
      (signal: Signal[_]) => {
        val r = ttp(state, signal)
        state = r._1
        r._2
      }
    }
  }

  implicit class RichSimpleSignalProcessor(sp: SimpleSignalProcessor) {
    def toTransducer[TInput, TOutput](input: Contact[TInput], output: Contact[TOutput]) = {
      data: TInput =>
        val inputSignal = Signal(input, data)
        val outputSignals = sp(inputSignal)
        outputSignals.collect {
          case Signal(`output`, outputData) => outputData.asInstanceOf[TOutput]
        }
    }

    def toMapTransducer[TInput, TOutput](input: Contact[TInput], output: Contact[TOutput]) = {
      data: TInput =>
        val inputSignal = Signal(input, data)
        val outputSignals = sp(inputSignal)
        val outputs = outputSignals.collect {
          case Signal(`output`, outputData) => outputData.asInstanceOf[TOutput]
        }
        if (outputs.isEmpty)
          throw new IllegalStateException(s"Cannot convert empty output results $outputs from $output to List(data).")
        val result = outputs.head
        if (outputs.tail.nonEmpty)
          throw new IllegalStateException(s"Cannot convert multiple output results $outputs from $output to List(data).")
        result
    }

  }


}
