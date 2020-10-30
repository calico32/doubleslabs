package dev.wiisportsresorts.doubleslabs

import org.bukkit.Bukkit
import org.bukkit.GameMode.SURVIVAL
import org.bukkit.block.Block
import org.bukkit.block.data.type.Slab
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.util.Vector

fun isDoubleSlab(block: Block?): Boolean {
    block ?: return false
    return (block.type.toString().contains("SLAB") && (block.blockData as Slab).type == Slab.Type.DOUBLE)
}

infix fun ClosedRange<Double>.step(step: Double): Iterable<Double> {
    require(start.isFinite())
    require(endInclusive.isFinite())
    require(step > 0.0) { "Step must be positive, was: $step." }
    val sequence = generateSequence(start) { previous ->
        if (previous == Double.POSITIVE_INFINITY) return@generateSequence null
        val next = previous + step
        if (next > endInclusive) null else next
    }
    return sequence.asIterable()
}

@Suppress("unused")
class DoubleSlabs : JavaPlugin(), Listener {
    override fun onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this)
        Bukkit.getLogger().info("[DoubleSlabs] Plugin enabled!")
    }

    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) {
        event.isCancelled && return
        val block = event.block
        val player = event.player

        !player.hasPermission("doubleslabs.use") && !player.isOp && return

        if (isDoubleSlab(event.block)) {
            event.isCancelled = true

            var blockVector: Vector? = null

            event.player.eyeLocation

            val looking = player.location.toVector().clone().add(Vector(0.0, player.eyeHeight, 0.0))

            for (d in 1.0..6.0 step 0.05) {
                val multiplied = looking.clone().add(player.location.direction.clone().multiply(d))
                val location = multiplied.toLocation(player.world)
                if (location.blockX == block.location.blockX && location.blockZ == block.location.blockZ) {
                    blockVector = multiplied
                    break
                }
            }

            val newData = block.blockData as Slab
            newData.type = if (blockVector != null && blockVector.y - block.location.blockY < .5) Slab.Type.TOP else Slab.Type.BOTTOM
            block.blockData = newData

            if (player.gameMode == SURVIVAL) block.world.dropItemNaturally(block.location, ItemStack(block.type, 1))
        }
    }
}

