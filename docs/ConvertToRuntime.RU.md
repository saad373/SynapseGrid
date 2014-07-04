Конвертация систем и компонентов в runtime-системы
==================================================

Статическое описание системы не может быть использовано непосредственно для обработки данных.
Необходим интерпретатор, который будет выполнять логику перемещения данных и вызывать пользовательские
функции в правильном порядке. По мере развития библиотеки стали появляться новые типы системных
компонентов, что потребовало проведения декомпозиции интерпретатора "SignalProcessor" и привело к
формированию концепции RuntimeComponent'ов. Концепция заключается в том, что логика интерпретации
разных типов компонентов инкапсулирована в отдельных RuntimeComponent'ах.

RuntimeComponent'ы похожи на простые компоненты, но отличаются тем, что действуют в условиях осведомлённости
о том, что фактически в системе обрабатываются сигналы, а не чистые данные. Поэтому, во-первых,
RuntimeComponent'ы используются в обработке сигналов непосредственно, а, во-вторых, RuntimeComponent'ы
имеют более широкие возможности в плане организации вычислений.

Поддерживаются следующие сигнатуры RuntimeComponent'ов:
 1. Signal => List[Signal] (RuntimeComponentFlatMap) простой runtime-компонент, не работающий
    с внешним состоянием.
 2. StateValue, Signal => StateValue, List[Signal] (RuntimeComponentStateFlatMap) - runtime-компонент,
    имеющий доступ к одному состоянию, которое он может изменить. В параллельном случае
    исполнение этого компонента сериализуется так, чтобы к состоянию в каждый момент времени имел доступ
    только один поток.
 3. List[StateValue], Signal => List[StateValue], List[Signal] (RuntimeComponentMultiState) - наиболее общий
    случай runtime-компонента, который имеет доступ сразу к нескольким состояниям. В функцию, связанную
    с этим компонентом, передаётся Context (map), содержащий значения указанных переменных состояния, а
    по окончании выполнения функции в общий контекст передаются изменённые значения состояний.

StaticSystem содержит пользовательские функции, привязанные к контактам. Сами функции ничего не знают о том,
что обработка происходит с использованием сигналов. Поэтому требуется выполнить конвертацию всех
пользовательских компонентов в Runtime-компоненты. Для простых компонентов, наподобие Link'ов, конвертация
делается очень просто. Достаточно распаковать сигнал, передать данные в пользовательскую функцию, а результат
снова упаковать в сигналы.

Конвертация "красных" стрелочек
-------------------------------
Особый случай представляют собой так называемые "красные" стрелочки RedLinks. В некоторых случаях
равномерное (по тактам) выполнение процесса обработки сигналов неудобно, и требуется
ряд операций выполнить за один такт. Для этой цели может быть создана вложенная подсистема, выполнение
которой всегда осуществляется за один такт родительской системы. Но создание подсистемы является
громоздкой операцией, к тому же, происходит изоляция от состояний родительской системы. Легковесным
способом ускоренного выполнения действий являются красные стрелки. Семантика красных стрелок
такова, что они запускают вложенную обработку сигналов на той же самой системе. Обработка других сигналов
приостанавливается до тех пор, пока сигнал, попавший на красную стрелку, и все порождённые сигналы
не достигнут множества зелёных контактов. Все сигналы, порождённые на зелёных контактах, появляются в
следующем такте родительской обработки сигналов.

Такая концепция красных стрелочек приводит к тому, что при конвертации красных стрелочек в RuntimeComponbent'ы
требуется специальный тип - RuntimeComponentMultiState. Можно было бы отказаться от красных стрелочек, но пока
можно считать, что это интересная возможность и техническое решение для конвертации существует.

Конвертация подсистем
---------------------

Каждая подсистема может быть представлена инкапсулированным Runtime Component'ом, в том случае, если
вложенные подсистемы будут также сконвертированы в RuntimeComponent'ы и вложены внутрь верхнего
компонента. Такой подход достаточно просто реализуется путём рекурсивного применения конвертеров.

Иногда, однако, вложенные компоненты не могут быть инкапсулированы. Если, к примеру,
вложенный компонент располагается на другом компьютере. На первом узле надо расположить прокси-объект, который
будет пересылать сообщения на второй узел, а на втором узле надо пропустить
создание родительских компонентов, и вместо них создать прокси-объект.

Рекурсивная конвертация для такого сценария не подходит, т.к. на каждом уровне рекурсии должен возвращаться
объект-RuntimeComponent.

Конвертация систем с экторами
-----------------------------


Нерекурсивная конвертация подсистем(*)
-----------------------------------

RuntimeComponent имеет такую особенность - входные и выходные контакты находятся на одном уровне иерархии. Если
несколько расширить определение RuntimeComponent'а и дать возможность обработки сигналов на разных уровнях иерархии,
это позволит отделить обработку сигналов внутри системы от передачи в другие подсистемы (включая дочерние).
Модифицированный интерфейс компонента будем называть PRuntimeComponent.

    type PRuntimeComponent = PSignal => PSignals

Здесь под PSignal'ом понимается сигнал на контакте с учётом пути этого контакта:

    PContact(Path, Contact)

    PSignal(PContact, data)

Расширение интерфейса PRuntimeComponent на контакты с учётом их пути позволяет ограничивать вычисления внутри систем
в произвольной точке и имеющиеся сигналы передавать в другую точку для их обработки. Кроме непосредственной цели
обособления подсистем, мы получаем возможность обособления обработки произвольных частей систем.
