package invizio.cip.filter;

import java.util.Vector;

import net.imglib2.Cursor;
import net.imglib2.Interval;
import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.multithreading.Chunk;
import net.imglib2.multithreading.SimpleMultiThreading;
import net.imglib2.type.Type;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

@SuppressWarnings("deprecation")
public class TempUtils {


/**
 * 
 * Reused from imglib2 algorithm morphology package
 *
 */

	
	
	static < T extends Type< T > > Img< T > copyCropped( final Img< T > largeSource, final Interval interval, final int numThreads )
	{
		final long[] offset = new long[ largeSource.numDimensions() ];
		for ( int d = 0; d < offset.length; d++ )
		{
			offset[ d ] = ( largeSource.dimension( d ) - interval.dimension( d ) ) / 2;
		}
		final Img< T > create = largeSource.factory().create( interval, largeSource.firstElement().copy() );

		final Vector< Chunk > chunks = SimpleMultiThreading.divideIntoChunks( create.size(), numThreads );
		final Thread[] threads = SimpleMultiThreading.newThreads( numThreads );
		for ( int i = 0; i < threads.length; i++ )
		{
			final Chunk chunk = chunks.get( i );
			threads[ i ] = new Thread( "Morphology copyCropped thread " + i )
			{
				@Override
				public void run()
				{
					final IntervalView< T > intervalView = Views.offset( largeSource, offset );
					final Cursor< T > cursor = create.cursor();
					cursor.jumpFwd( chunk.getStartPosition() );
					final RandomAccess< T > randomAccess = intervalView.randomAccess();
					for ( long step = 0; step < chunk.getLoopSize(); step++ )
					{
						cursor.fwd();
						randomAccess.setPosition( cursor );
						cursor.get().set( randomAccess.get() );
					}
				}
			};
		}

		SimpleMultiThreading.startAndJoin( threads );
		return create;
	}
	
	
	
}
