package actor

import domain.ReadingStatus
import org.apache.pekko.actor.testkit.typed.scaladsl.ActorTestKit
import org.scalatest.BeforeAndAfterAll
import org.scalatest.wordspec.AnyWordSpec

class StatsActorSpec
  extends AnyWordSpec
    with BeforeAndAfterAll:

  val testKit = ActorTestKit()

  override def afterAll(): Unit =
    testKit.shutdownTestKit()

  "StatsActor" should {

    "return initialized stats" in {

      val actor =
        testKit.spawn(StatsActor())

      val probe =
        testKit.createTestProbe[StatsActor.Stats]()

      actor ! StatsActor.Initialize(
        StatsActor.Stats(4, 0, 0)
      )

      actor ! StatsActor.GetStats(probe.ref)

      val result =
        probe.receiveMessage()

      assert(result.wantToRead == 4)
      assert(result.reading == 0)
      assert(result.finished == 0)
    }

    "change statistics when book status changes" in {

      val actor =
        testKit.spawn(StatsActor())

      val probe =
        testKit.createTestProbe[StatsActor.Stats]()

      actor ! StatsActor.Initialize(
        StatsActor.Stats(4, 0, 0)
      )

      actor ! StatsActor.StatusChanged(
        ReadingStatus.WantToRead,
        ReadingStatus.Reading
      )

      actor ! StatsActor.GetStats(probe.ref)

      val result =
        probe.receiveMessage()

      assert(result.wantToRead == 3)
      assert(result.reading == 1)
      assert(result.finished == 0)
    }

    "move book from reading to finished" in {

      val actor =
        testKit.spawn(StatsActor())

      val probe =
        testKit.createTestProbe[StatsActor.Stats]()

      actor ! StatsActor.Initialize(
        StatsActor.Stats(2, 3, 1)
      )

      actor ! StatsActor.StatusChanged(
        ReadingStatus.Reading,
        ReadingStatus.Finished
      )

      actor ! StatsActor.GetStats(probe.ref)

      val result =
        probe.receiveMessage()

      assert(result.wantToRead == 2)
      assert(result.reading == 2)
      assert(result.finished == 2)
    }
  }