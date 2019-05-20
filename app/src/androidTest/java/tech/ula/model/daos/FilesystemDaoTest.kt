package tech.ula.model.daos

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import android.database.sqlite.SQLiteConstraintException
import androidx.test.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import org.junit.After
import org.junit.Assert.* // ktlint-disable no-wildcard-imports
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import tech.ula.model.repositories.UlaDatabase
import tech.ula.model.entities.Filesystem

@RunWith(AndroidJUnit4::class)
class FilesystemDaoTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var db: UlaDatabase

    @Before
    fun initDb() {
        db = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getContext(),
                UlaDatabase::class.java)
                .allowMainThreadQueries()
                .build()
    }

    @After
    fun closeDb() = db.close()

    // Filesystem tests
    @Test(expected = SQLiteConstraintException::class)
    fun dbEnforcesUniqueFsIdConstraint() {
        val fs1 = Filesystem(1)
        val fs2 = Filesystem(1)
        db.filesystemDao().insertFilesystem(fs1)
        db.filesystemDao().insertFilesystem(fs2)
    }

    @Test
    fun insertFilesystemAndGetByName() {
        val inserted = Filesystem(DEFAULT_NON_AUTOGENERATED_ID, name = DEFAULT_NAME)
        db.filesystemDao().insertFilesystem(inserted)
        val retrieved = db.filesystemDao().getFilesystemByName(DEFAULT_NAME)
        assertNotNull(retrieved)
        assertEquals(inserted, retrieved)
    }

    @Test
    fun deleteFilesystemAndFailRetrieval() {
        db.filesystemDao().insertFilesystem(DEFAULT_FILESYSTEM)
        val id = db.filesystemDao().getFilesystemByName(DEFAULT_NAME).id
        db.filesystemDao().deleteFilesystemById(id) // DB autoincrements the id
        assertNull(db.filesystemDao().getAllFilesystems().value)
        assertNull(db.filesystemDao().getFilesystemByName(DEFAULT_NAME))
    }

    @Test
    fun updateFilesystem() {
        val fs = Filesystem(DEFAULT_NON_AUTOGENERATED_ID, name = "start")
        db.filesystemDao().insertFilesystem(fs)
        assertEquals(fs, db.filesystemDao().getFilesystemByName(fs.name))

        fs.name = "end"
        db.filesystemDao().updateFilesystem(fs)
        assertEquals(fs, db.filesystemDao().getFilesystemByName(fs.name))
    }

    companion object {
        val DEFAULT_NAME = "test"
        val DEFAULT_FILESYSTEM = Filesystem(0, name = DEFAULT_NAME)

        val DEFAULT_NON_AUTOGENERATED_ID = 1L
    }
}