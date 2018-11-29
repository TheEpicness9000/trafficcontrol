package com.clussmanproductions.roadstuffreborn.tileentity;

import com.clussmanproductions.roadstuffreborn.ModSounds;
import com.clussmanproductions.roadstuffreborn.blocks.BlockCrossingGateGate;
import com.clussmanproductions.roadstuffreborn.blocks.BlockCrossingGateLamps;
import com.clussmanproductions.roadstuffreborn.util.ILoopableSoundTileEntity;
import com.clussmanproductions.roadstuffreborn.util.LoopableTileEntitySound;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.audio.SoundManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import scala.Tuple3;

public class CrossingGateGateTileEntity extends TileEntity implements ITickable, ILoopableSoundTileEntity {
	private float gateRotation = -60;
	private double gateDelay = 0;
	private EnumStatuses status = EnumStatuses.Open;
	@SideOnly(Side.CLIENT)
	private ISound gateSound;
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setFloat("gateRotation", gateRotation);
		compound.setDouble("gateDelay", gateDelay);
		compound.setInteger("status", getCodeFromEnum(status));
		return super.writeToNBT(compound);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		gateRotation = compound.getFloat("gateRotation");
		gateDelay = compound.getDouble("gateDelay");
		status = getStatusFromCode(compound.getInteger("status"));
	}
	
	public float getFacingRotation()
	{
		IBlockState blockState = world.getBlockState(getPos());
		
		EnumFacing facing = blockState.getValue(BlockCrossingGateGate.FACING);
		switch(facing)
		{
			case NORTH:
				return 0;
			case WEST:
				return -270;
			case SOUTH:
				return -180;
			case EAST:
				return -90;
			default:
				return 0;
		}
	}
	
	public Tuple3<Double, Double, Double> getTranslation(double x, double y, double z)
	{
		IBlockState blockState = world.getBlockState(getPos());
		
		EnumFacing facing = blockState.getValue(BlockCrossingGateGate.FACING);
		switch(facing)
		{
			case NORTH:
				return new Tuple3<Double, Double, Double>(x + 0.75, y + 0.125, z + 0.5);
			case EAST:
				return new Tuple3<Double, Double, Double>(x + 0.5, y + 0.125, z + 0.75);
			case SOUTH:
				return new Tuple3<Double, Double, Double>(x + 0.25, y + 0.125, z + 0.5);
			case WEST:
				return new Tuple3<Double, Double, Double>(x + 0.5, y + 0.125, z + 0.25);
			default:
				return new Tuple3<Double, Double, Double>(x + 0.75, y + 0.125, z + 0.5);
		}
	}
	
	public float getGateRotation()
	{
		return gateRotation;
	}
	
	public AxisAlignedBB getBoundingBox()
	{
		if (status != EnumStatuses.Closed)
		{
			return new AxisAlignedBB(0, 0, 0, 1, 1, 1);
		}
		
		double startX = 0, startZ = 0, x = 0, z = 0;
		
		EnumFacing facing = world.getBlockState(pos).getValue(BlockCrossingGateGate.FACING);
		switch(facing)
		{
			case NORTH:
				x = -3;
				startX = 1;
				z = 1;
				startZ = 0;
				break;
			case SOUTH:
				x = 4;
				z = 1;
				break;
			case EAST:
				x = 1;
				z = -3;
				startZ = 1;
				break;
			case WEST:
				x = 1;
				z = 4;
			default:
				break;
		}
		
		return new AxisAlignedBB(startX, 0, startZ, x, 1, z);
	}
	
	public void setStatusByIsPowered(Boolean powered)
	{
		if (powered && status != EnumStatuses.Closed)
		{
			status = EnumStatuses.Closing;
		}
		else if (status != EnumStatuses.Open)
		{
			status = EnumStatuses.Opening;
		}
	}
	
	private int getCodeFromEnum(EnumStatuses status)
	{
		switch(status)
		{
			case Closed:
				return 0;
			case Closing:
				return 1;
			case Open:
				return 2;
			case Opening:
				return 3;
			default:
				return -1;
		}
	}
	
	private EnumStatuses getStatusFromCode(int code)
	{
		switch(code)
		{
			case 0:
				return EnumStatuses.Closed;
			case 1:
				return EnumStatuses.Closing;
			case 2:
				return EnumStatuses.Open;
			case 3:
				return EnumStatuses.Opening;
			default:
				return null;
		}
	}
	
	public enum EnumStatuses
	{
		Open,
		Closing,
		Closed,
		Opening
	}

	@Override
	public void update() {
		if (world.isRemote)
		{
			return;
		}
		
		switch(status)
		{
			case Closing:
				if (gateDelay < 80)
				{
					gateDelay++;
					sendUpdates(true);
					return;
				}
				
				if (gateRotation >= 0)
				{
					status = EnumStatuses.Closed;
					sendUpdates(true);
					return;
				}
				
				gateRotation += 0.5F;				
				sendUpdates(true);
				break;
			case Opening:
				if (gateRotation <= -60)
				{
					gateDelay = 0;
					status = EnumStatuses.Open;
					sendUpdates(true);
				}
				
				gateRotation -= 0.5F;
				sendUpdates(true);
				break;
			default:
				return;
		}
	}
	
	private void sendUpdates(Boolean markDirty)
	{
		if (markDirty)
		{
			markDirty();
		}
		
		world.markBlockRangeForRenderUpdate(pos, pos);
		world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
		world.scheduleBlockUpdate(pos, getBlockType(), 0, 0);
	}
	
	@Override
	public NBTTagCompound getUpdateTag() {
		NBTTagCompound nbt = super.getUpdateTag();
		nbt.setFloat("gateRotation", gateRotation);
		nbt.setInteger("status", getCodeFromEnum(status));
		
		return nbt;
	}
	
	@Override
	public void handleUpdateTag(NBTTagCompound tag) {
		gateRotation = tag.getFloat("gateRotation");
		status = getStatusFromCode(tag.getInteger("status"));
		
		if (world.isRemote && status == EnumStatuses.Opening || (status == EnumStatuses.Closing && gateRotation > -60))
		{
			handlePlaySound();
		}
	}
	
	@SideOnly(Side.CLIENT)
	public void handlePlaySound()
	{
		if (gateSound == null)
		{
			gateSound = new LoopableTileEntitySound(ModSounds.gateEvent, this, pos, 0.3f, 1);
		} 
		
		SoundHandler handler = Minecraft.getMinecraft().getSoundHandler();
		if (handler != null && !handler.isSoundPlaying(gateSound))
		{
			handler.playSound(gateSound);
		}
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		return new SPacketUpdateTileEntity(getPos(), 1, getUpdateTag());
	}
	
	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		NBTTagCompound nbt = pkt.getNbtCompound();
		handleUpdateTag(nbt);
	}

	@Override
	public boolean isDonePlayingSound() {
		return status == EnumStatuses.Closed || status == EnumStatuses.Open;
	}

	public EnumStatuses getStatus()
	{
		return status;
	}
}
