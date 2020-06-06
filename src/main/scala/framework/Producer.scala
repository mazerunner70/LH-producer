package demo.kafka.scalad

import java.util.Properties

import framework.KafkaRecord

import scala.collection.JavaConverters._
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord, RecordMetadata}

import scala.concurrent.Future
import scala.reflect.runtime.universe.{TypeTag, typeOf}
import scala.util.{Failure, Success}

class Producer[K: TypeTag, V: TypeTag] {

  val ser = Map(
    "String" -> "org.apache.kafka.common.serialization.StringSerializer",
    "Int" -> "org.apache.kafka.common.serialization.IntegerSerializer"
  )


  var kafkaProducer: Option[KafkaProducer[K, V]] = None

  def initialise() = {
    val consumerProps = Map(
      "bootstrap.servers"  -> "kafka0:29092",
      "key.serializer"   -> ser.getOrElse(typeOf[K].toString, ""),
      "value.serializer" -> ser.getOrElse(typeOf[V].toString, ""),
    )
    val props = new Properties()
    props.putAll(consumerProps.view.mapValues(_.toString).toMap.asJava)
    kafkaProducer = Some( new KafkaProducer[K, V](props))
  }

  def produce(kafkaRecord: KafkaRecord[K, V]) = {
    import scala.concurrent.ExecutionContext.Implicits.global
    println(kafkaRecord)
    val record = new ProducerRecord[K, V](kafkaRecord.topic, kafkaRecord.key, kafkaRecord.value)
    val recordmetadata: RecordMetadata = kafkaProducer match {
      case Some(producer) => producer.send(record).get()
      case None => throw UninitializedFieldError("must initialise")
    }
    println(recordmetadata)
  }

}

object Producer {
  def apply[K: TypeTag, V: TypeTag](): Producer[K, V] = {
    val producer = new Producer[K, V]()
    producer.initialise()
    producer
  }
}
