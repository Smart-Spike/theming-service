package theming.repositories

import java.util.UUID

object IdGenerator {

  def generateId: String = UUID.randomUUID().toString

}
